package Controller;

import View.MapCanvas;
import View.POIpopup;
import Model.Config;
import Model.Model;
import Model.NoPathFoundException;
import Model.POI;
import Model.UserPOI;
import Model.Vertex;
import Model.Road;
import java.awt.Cursor;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JOptionPane;
import javax.swing.Timer;

/**
 * Creates a MapCanvas
 * Provides functionality to interpret mouseinput as pan and zoom commandos, as well as keyboard input.
 */
public class MapController extends MouseAdapter implements KeyListener, ActionListener {
	private Model model;
	private Rectangle2D clip; //The model bounds.
	private MapCanvas canvas;  
	private NavController navController;  //Reference for updating content in NavScreen's different lists
	private boolean AAon, addPOIMode, closestRoadDrawn, alphaOn, dragStart, dragEnd, dragPoint, popupActive;
	private int mx, my, rotates, screenWidth;   //Mouse position and number of times rotated, and screenWidth at startup
	private double mapWidth, mapHeight;
	private Timer aaTimer;   //Swing timer used as delay for AA during zoom.
	private Timer mouseMoveTimer; //Swing timer used as delay for closest road_lvl1 function.
	private double MAX_ZOOM_X, MAX_ZOOM_Y; //Maximal map width, used for locking zoom to model.
	private final double MIN_ZOOM; //Minimal map width in meters.
	private Point2D anchor, currentMousePosition, currentModelMousePosition, clickPoint, draggingPOI;
	private Vertex closestVertexBefore;

	/**
	 * Contructs a MapCanvas
	 * @param nav The NavController used by the program
	 * @param width The desired width of the MapCanvas in pixels
	 * @param height The desired height of the MapCanvas in pixels
     */
	public MapController(NavController nav, int width, int height) {
		//Assign model and view to work with
		model = Main.model();
		canvas = new MapCanvas(width, height);
		navController = nav;

		//Initialize vars with default setting
		screenWidth = canvas.getWidth();
		AAon = true;
		alphaOn = true;
		addPOIMode = false;
		closestRoadDrawn = false;
		dragStart = false;
		dragEnd = false;
		closestVertexBefore = null;
		popupActive = false;

		//Sets up listeners for the canvas
		canvas.addMouseListener(this);    
		canvas.addMouseWheelListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addKeyListener(this);

		//Clip defines model bounds. Used to limit panning and zooming.
		clip = new Rectangle2D.Double(model.minLon(), model.maxLat(), (model.maxLon()-model.minLon()), (model.minLat()-model.maxLat()));
		MAX_ZOOM_X = Math.abs(model.minLon() - model.maxLon());
		MAX_ZOOM_Y = Math.abs(model.maxLat() - model.minLat());
		MIN_ZOOM = 50 / model.METER_CONVERSION;

		//Zoom to the mapcoordinates
		resetView();

		//Avoid nullpointers
		currentMousePosition = new Point2D.Float(0.0f, 0.0f);
		currentModelMousePosition = new Point2D.Float(0.0f, 0.0f);
		clickPoint = new Point2D.Float(0.0f, 0.0f);

		//Initialize timer object for zoom-noAA use.
		aaTimer = new Timer(100, this);  
		aaTimer.setRepeats(false);
		aaTimer.start();

		//Initialize timer object for closest road.
		mouseMoveTimer = new Timer(800, this); 
		mouseMoveTimer.setRepeats(false);
		mouseMoveTimer.start();
	}

	/**
	 * Resets transform and brings standardview of model in focus. 
	 */
	public void resetView() {
		canvas.resetTransform();
		canvas.pan(-model.minLon(), -model.maxLat());
		double canvasLength = Math.max(canvas.getWidth(), canvas.getHeight());
		double modelLength = Math.min(clip.getWidth(), clip.getHeight());
		canvas.zoom(canvasLength/modelLength,0,0);
		updateZoomLevel();
		updateClip();
		model.update();
	}

	/**
	 * Forces MapCanvas to update it's content based on what is in view.
	 */
	private void updateClip() {
		canvas.viewport(canvas.getViewPort());
	}

	/**
	 * Pans the view to the closest allowed position
	 * @param dx The desired x-coordinate of the pan in view-coordinates
	 * @param dy The desired y-coordinate of the pan in view-coordinates
     */
	public void pan(double dx, double dy) {
		if(!panWithCorrection(dx, dy)){
			canvas.pan(dx, dy); //Pan as normal if no correction was needed
		}
		updateClip();
		model.update();
	}

	/**
	 * Pan with respect to borders.
	 * Checks whether the proposed pan is within the model's bounds. If not
	 * it will recalculate to a legal value and pan with that instead
	 * effectively limiting view from drifting away from the model.
	 * @param dx: Horizontal panning amount proposed
	 * @param dy: Vertical panning amount proposed
	 * @return True if panned with correction (carries out the pan itself)
	 *         False, if no correction is necessary (Will not pan itself)
	 */
	private boolean panWithCorrection(double dx, double dy){
		Rectangle2D next = canvas.getViewPort(dx, dy);
		if (!clip.contains(next)) {
			while(next.getWidth() > MAX_ZOOM_X || next.getHeight() > MAX_ZOOM_Y){
				//If the view is somehow zoomed too much out it zooms in until it fits the bounds
				canvas.zoom(1.1, canvas.getWidth()/2, canvas.getHeight()/2);
				next = canvas.getViewPort(dx, dy);
			}
			Rectangle2D current = canvas.getViewPort();
			double difX = dx;
			double difY = dy;
			double dif; 
			double screenFactor = canvas.getWidth() / next.getWidth();

			//Calculate legal x
			if (next.getMinX() < clip.getMinX()) {
				dif = current.getX() - clip.getX();
				difX = dif * screenFactor;
			} else if (next.getMaxX() > clip.getMaxX()) {
				dif = current.getMaxX() - clip.getMaxX();
				difX = dif * screenFactor;
			}

			//Calculate legal y
			if (next.getMinY() < clip.getMinY()) {
				dif = current.getY() - clip.getY();
				difY = dif * screenFactor;
			} else if (next.getMaxY() > clip.getMaxY()) {
				dif = current.getMaxY() - clip.getMaxY();
				difY = dif * screenFactor;
			}

			//Rotates to default for an easier panning
			canvas.rotate(rotates * (-Math.PI / 20), canvas.inverse(canvas.getWidth() / 2, canvas.getHeight() / 2));
			canvas.pan(difX, difY);
			canvas.rotate(rotates * (Math.PI / 20), canvas.inverse(canvas.getWidth() / 2, canvas.getHeight() / 2));
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Zoom around the center of screen
	 */
	public void zoom(double zoomFactor) {
		zoom(zoomFactor, canvas.getWidth()/2, canvas.getHeight()/2);
	}

	/**
	 * Zoom with respect to minimum and maximum zoom-levels.
	 * Uses a center point defined by dx & dy, and then enlarges by var zoomfactor.
	 * If the zoom results in anything higher than max or below min, it will stop.
	 * Relies on panWithCorrection to correct view when zooming near borders.
	 * @param zoomFactor: The amount to enlarge map. (1.0 is neutral, <1 zooms out, >1 zooms in)
	 * @param dx: X-coordinate of zooming centerpoint
	 * @param dy: Y-coordinate of zooming centerpoint
	 */
	public void zoom(double zoomFactor, double dx, double dy) {
		//Calculate the view after proposed zoom-operation.
		double nextZoomX = mapWidth / zoomFactor;
		double nextZoomY = mapHeight / zoomFactor;
		//Evaluate against set Max&Min zoom values.
		boolean aboveMin = nextZoomX > MIN_ZOOM && nextZoomY > MIN_ZOOM;
		boolean belowMax = nextZoomX < MAX_ZOOM_X && nextZoomY < MAX_ZOOM_Y;
		//If within bounds or about to go inside bounds, allow zoom operation.
		if ((aboveMin && belowMax) || (!aboveMin && zoomFactor < 1.0) || (!belowMax && zoomFactor > 1.0)) {
			canvas.zoom(zoomFactor, dx, dy);
			panWithCorrection(0,0); //Realigns map if zoomed near a border.
			updateZoomLevel(); //Recalculates vars related to zoom. Ex the bottom scale bar.
			updateClip();
			model.update();
		}
	}

	/**
	 * Rotates the view around the center of the screen
	 * @param toRight Which direction is rotated
	 */
	public void rotate(boolean toRight){
		rotate(toRight, canvas.inverse(canvas.getWidth() / 2, canvas.getHeight() / 2));
	}

	/**
	 * Rotates the view around the anchor
	 * @param toRight Which direction is rotated
	 * @param anchor The anchor which the screen is rotated around
	 */
	private void rotate(boolean toRight, Point2D anchor) {
		double amount;
		if(toRight){
			amount = (Math.PI / 20);
		} else {
			amount = -(Math.PI / 20);
		}
		if(clip.contains(canvas.getRotateViewPort(amount, anchor))) {
			if(amount > 0){ rotates++; } 
			else { rotates--; }	
		} else {
			zoom(1.25);
			panWithCorrection(0,0);
			if(amount > 0){ rotates++; } 
			else { rotates--; }	
		}
		canvas.rotate(amount, anchor);
		updateClip();
		model.update();
	}

	/**
	 * Toggles adding of POI. Mouse changes to a star and mouseevents will be handled differently.
	 * Pan, zoom & rotate is disabled when adding a point.
	 */
	public void addingMode(){ addPOIMode = !addPOIMode; }
	
	/**
	 * Notifies the Controller that the popup is no longer active
	 */
	public void notifyPopupClosed() { popupActive = false; }

	/**
	 * Calculates the diagonal distance in meters between two points
	 * @param p1 The first point
	 * @param p2 The second point
     * @return The distance between the points in meters
     */
	public double calcDistMeters(Point2D p1, Point2D p2) {
		double distance = calcDist(p1, p2);
		distance *= Model.METER_CONVERSION; //Convert lat/lon dist to meters (!Approx!)
		return distance;
	}

	/**
	 * Calculates the diagonal distance in meters between two points
	 * @param p1 The first point
	 * @param p2 The second point
	 * @return The distance between the points
	 */
	public double calcDist(Point2D p1, Point2D p2) {
		double a = p2.getX()-p1.getX(); 
		double b = p1.getY()-p2.getY();
		return Math.hypot(a, b);
	}
		
	/**
	 * Zooms to a specified point in model. 
	 * @param point		The point that will be in focus after zoom
	 * @param mapWidth 	The wished amount of map-width visible around the point.  
	 */
	public void zoomToPoint(Point2D point, int mapWidth) {
		if(point == null) { return; }

		//Use procedure a la resetView(), but instead of model bounds, use point's.
		canvas.resetTransform();
		canvas.pan(-point.getX(), -point.getY());
		if(mapWidth < MIN_ZOOM) { mapWidth = (int) MIN_ZOOM; }  //Adjust to avoid zooming infinitly in.
		Rectangle2D tmpViewPort = canvas.getViewPort();
		double zAmount = Math.min(tmpViewPort.getWidth() * Model.METER_CONVERSION, tmpViewPort.getHeight() * Model.METER_CONVERSION)/mapWidth;
		canvas.zoom(zAmount,0,0);
		canvas.pan(canvas.getWidth()/2, canvas.getHeight()/2);
		panWithCorrection(0,0);
		updateZoomLevel(); //Recalculate zoom-related vars, ex scalebar in view
		updateClip();
		model.update();
	}
	
	/**
	 * Recalculates zoom-level information, like detailLevel and sample-distance. 
	 */
	private void updateZoomLevel() {
		Rectangle2D viewPort = canvas.getViewPort();
		mapWidth = viewPort.getWidth();
		mapHeight = viewPort.getHeight();
		updateDetailLevel();
		calcSampleDist();
	}
	
	/**
	 * Set view-detail level based on the calculated width of a portion of screen in approx meters.
	 * Used by the MapCanvas to determine what to draw
	 */
	private void updateDetailLevel() {
		Double pixelFactor = calcDistMeters(canvas.inverse(0, 0), canvas.inverse(screenWidth * 0.35, 0));;
		if(pixelFactor < 150) {canvas.detailLevel(14);} //POI are shown
		else if(pixelFactor < 200) {canvas.detailLevel(13);} //Cycleways and walkways are shown
		else if(pixelFactor < 400) {canvas.detailLevel(12);} //Parking is shown
		else if(pixelFactor < 550) {canvas.detailLevel(11);} //Buildings are shown
		else if(pixelFactor < 700) {canvas.detailLevel(10);} //All roads are now shown
		else if(pixelFactor < 1200) {canvas.detailLevel(9);} //Airways and residential roads are shown
		else if(pixelFactor < 1500) {canvas.detailLevel(8);} //Grass is solid
		else if(pixelFactor < 1800) {
			if(alphaOn) {
				canvas.detailLevel(7); //Grass is fading in
				canvas.grassA((int) (1800 - pixelFactor) * 255 / 300);
			} else {
				canvas.detailLevel(8);
			}
		}
		else if(pixelFactor < 3000) {canvas.detailLevel(6);} //Sand is drawn
		else if(pixelFactor < 3500) {canvas.detailLevel(5);} //Farmland and plains solid
		else if(pixelFactor < 4500) {
			if(alphaOn) {
				canvas.detailLevel(4); //Waterways and More big roads are shown. Farmland and plains is fading in
				canvas.farmPlainA((int) (4500 - pixelFactor) * 255 / 1000);
			} else {
				canvas.detailLevel(5);
			}
		}
		else if(pixelFactor < 8000) {canvas.detailLevel(3);} //Railways are shown
		else if(pixelFactor < 15000) {canvas.detailLevel(2);} //Forest && residential areas are shown
		else {canvas.detailLevel(1);} //Only coasts and big roads are shown
	}
	
	/**
	 * Updates the numbers for the scalebar in the bottom right corner of the MapCanvas.
	 * Takes a 100px sample from view and supplies the rounded distance in meters.
	 */
	private void calcSampleDist() {
		//Calculate 100px distance of screenspace in meters.
		Point2D start = canvas.inverse(0, 0);
		Point2D end = canvas.inverse(100, 0);
		double sampleDist = calcDistMeters(start, end);

		//Round the result depending on its size
		if(sampleDist < 25) {canvas.scale((int) sampleDist);}
		else if(sampleDist < 150) {canvas.scale((int)(Math.round( sampleDist / 5) * 5));}
		else if(sampleDist < 1000) {canvas.scale((int)(Math.round( sampleDist / 10) * 10));}
		else if(sampleDist < 2500) {canvas.scale((int)(Math.round( sampleDist / 50) * 50));}
		else {canvas.scale((int)(Math.round( sampleDist / 100) * 100));}
	}
	
	/**
	 * Enables searchBox on the MapCanvas.
	 * Used to mark and describe a single search result.
	 * @param txt: The text that will be visible in the box
	 * @param point: The point the box should be anchored to. (Lon/Lat wise)
	 */
	public void showSearchBox(String txt, Point2D point) {
		canvas.enableSearchBox(txt, point);
		model.update();
	}
	
	/**
	 * Enables two searchBoxes on the MapCanvas.
	 * Used to mark and describe a two points for navigation.
	 * @param firstTxt The text the first box should show
	 * @param firstPoint The point the first box should be anchored to. (Lon/Lat wise)
	 * @param secondTxt The text the second box should show
	 * @param secondPoint The point the second box should be anchored to. (Lon/Lat wise)
	 */
	public void showSearchBox(String firstTxt, Point2D firstPoint, String secondTxt, Point2D secondPoint) {
		canvas.enableSearchBox(firstTxt, firstPoint, secondTxt, secondPoint);
		model.update();
	}

	/**
	 * Removes the searchboxes
	 */
	private void removeSearchBox() {
		canvas.disableSearchBox();
		model.update(); 
	}

	/**
	 * Adds a POI to the Model
	 * @param point The POI to be added
     */
	public void addPOI(POI point){
		if(point != null) {
			model.userPOI(point);
			model.update();
		}
		navController.updatePOIlist();
	}

	/**
	 * Returns the UserPOI closest to the given coordinates in view-coordinates
	 * @param x The x-value of the coordinates
	 * @param y The y-value of the coordinates
     * @return The closest UserPOI. Returns null if no UserPOI is within 20 px
     */
	private UserPOI closestPOI(int x, int y) {
		if(model.userPOI().size() > 0) {
			Point2D[] screenPoints = new Point2D[model.userPOI().size()];
			Point2D mousePoint = new Point2D.Float(x,y);
			Point2D closest = canvas.translatePointToViewCoords(model.userPOI().get(0));
			double minDist = calcDist(mousePoint, closest);
			int finalIndex = 0;
			for(int i = 0; i < screenPoints.length; i++) {
				screenPoints[i] = canvas.translatePointToViewCoords(model.userPOI().get(i)); //Transforms to screen cords
				double testDist = calcDist(mousePoint, screenPoints[i]);
				if(testDist < minDist) {
					finalIndex = i; 
					minDist = testDist;
				}
			} 
			if(minDist < 20) { 
				return (UserPOI) model.userPOI().get(finalIndex);
			} 
		}
		return null;
	}

	/**
	 *
	 * @param e
     */
	@Override
	public void mousePressed(MouseEvent e) 	{
		//Enable pan
		if (!addPOIMode) {
			mx = e.getX();
			my = e.getY();
			if (AAon) {
				canvas.disableAA();
			}
			//Enable drag-POI
			UserPOI closest = closestPOI(e.getX(), e.getY());
			if (closest != null) {
				dragPoint = true;
				draggingPOI = closest;
			} else if (model.pathExist()) {
				startDragAndDrop(new Point2D.Float(e.getX(), e.getY()));
			}
		}
	}

	/**
	 * Enables drag and drop if the point is close to a start or endpoint of the current route
	 * @param click The point representing the mouseposition
     */
	private void startDragAndDrop(Point2D click) {
		//Drag navigation route, if close enough
		clickPoint = click;

		Point2D pathStartPoint = model.pathFrom();
		Point2D pathEndPoint = model.pathTo();
		Point2D pathStartViewCoords = canvas.translatePointToViewCoords(pathStartPoint);
		Point2D pathEndViewCoords = canvas.translatePointToViewCoords(pathEndPoint);

		if (clickPoint.distance(pathEndViewCoords) < 30) {
			dragEnd = true;
		}
		if (clickPoint.distance(pathStartViewCoords) < 30) {
			dragStart = true;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(addPOIMode){
			showPOIPopup(e);
		} else {
			openClosePOI(e);
		}
	}

	/**
	 * Opens a POIPopup frame
	 * @param e The MouseEvent of the MouseClicked-method
     */
	private void showPOIPopup(MouseEvent e){
		canvas.addingPoint(null);
		addPOIMode = false;
		model.update();
		Point2D bounds = canvas.getLocationOnScreen();
		int x = (int) (bounds.getX() + e.getX());
		int y = (int) (bounds.getY() + e.getY());
		Point2D.Float location = (Point2D.Float) canvas.inverse(e.getX(), e.getY());
		if(!popupActive) {
			new POIpopup(x, y, this, (float)location.getX(), (float)location.getY());
			popupActive = true;
		}
	}

	/**
	 * Opens a popup for an existing POI close to the event
	 * @param e The MouseEvent of the mousepressed
     */
	private void openClosePOI(MouseEvent e) {
		//Open closest POI, if within 32px
		UserPOI closest = closestPOI(e.getX(), e.getY());
		if (closest != null) {
			Point2D bounds = canvas.getLocationOnScreen();
			int x = (int) (bounds.getX() + e.getX());
			int y = (int) (bounds.getY() + e.getY());
			if (!popupActive) {
				new POIpopup(x, y, this, closest);
				popupActive = true;
			}
		}
	}


	@Override
	public void mouseMoved(MouseEvent e) {
		//Remove closest-road info box
		if (closestRoadDrawn) {
			model.update();
			closestRoadDrawn = false;
		}

		//Update vars
		Point2D mousePoint = new Point2D.Float(e.getX(), e.getY());
		currentMousePosition = new Point2D.Float((float) mousePoint.getX(), (float) mousePoint.getY());
		currentModelMousePosition = canvas.inverse(mousePoint.getX(), mousePoint.getY());
		//If addPOIMode enabled, move POI
		if(addPOIMode) {
			canvas.addingPoint(mousePoint);
			model.update();
		} else {
			UserPOI closest = closestPOI(e.getX(), e.getY());
			if (closest != null) {
				canvas.setCursor(new Cursor(Cursor.HAND_CURSOR));
				navController.setCursor(new Cursor(Cursor.HAND_CURSOR));
			} else {
				canvas.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				if (navController == null) {
					navController = Main.navController();
				}
				navController.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

			if (model.pathExist()) {
				Point2D pathFrom = model.pathFrom();
				Point2D pathTo = model.pathTo();
				Point2D pathFromViewCoords = canvas.translatePointToViewCoords(pathFrom);
				Point2D pathToViewCoords = canvas.translatePointToViewCoords(pathTo);

				if (currentMousePosition.distance(pathFromViewCoords) < 30) {
					model.highlight(pathFromViewCoords);
				} else if (currentMousePosition.distance(pathToViewCoords) < 30) {
					model.highlight(pathToViewCoords);
				} else {
					model.highlight(null);
					canvas.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					if (navController == null) navController = Main.navController();
					navController.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
				model.update();
			}
		}
		mouseMoveTimer.restart();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		model.highlight(null);
		Point2D mousePoint = new Point2D.Float(e.getX(), e.getY());
		currentMousePosition = new Point2D.Float((float) mousePoint.getX(), (float) mousePoint.getY());
		currentModelMousePosition = canvas.inverse(mousePoint.getX(), mousePoint.getY());
		if(dragEnd || dragStart) {
			removeSearchBox();
			Road closestRoad;
			Vertex closestVertex = null;
			closestRoad = model.closestRoad(currentModelMousePosition);
			if (closestRoad != null) {
				closestVertex = closestRoad.closestVertex(currentModelMousePosition);
			}
			if (closestVertex != null && closestVertex != closestVertexBefore) {
				Point2D startPos = model.pathFrom();
				Point2D endPos = model.pathTo();
				Road[] path;
				try {
					if(dragStart) {
						model.pathFinder().drag(false, false);
						path = model.pathFinder().path(closestVertex.getPosition(), endPos, navController.carSelected(), navController.fastSelected());
						model.pathFrom(closestVertex);
						model.path(path);
					} else if(dragEnd) {
						model.pathFinder().drag(true, false);
						path = model.pathFinder().path(startPos, closestVertex.getPosition(), navController.carSelected(), navController.fastSelected());
						model.pathTo(closestVertex);
						model.path(path);
					}
				} catch (NoPathFoundException ex) {
					//Intentionally do nothing :)
				}
				closestVertexBefore = closestVertex;
			}
			model.update();
		} else if(addPOIMode) {
			canvas.addingPoint(currentMousePosition);
			model.update();
		} else if(dragPoint) {
			if(draggingPOI != null) { draggingPOI.setLocation(currentModelMousePosition); }
			model.update();
		} else {
			double dx = e.getX() - mx; //Find direction that is about to be panned
			double dy = e.getY() - my;
			pan(dx,dy);
			mx = e.getX();
			my = e.getY();
		}
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		//Stop highlighting dragpoint
		model.highlight(null);
		//ZOOM
		if(!addPOIMode) {
			canvas.disableAA();
			double zoomFactor = Math.pow(1.1, -e.getWheelRotation());
			zoom(zoomFactor, e.getX(), e.getY());
			aaTimer.restart();
		}
	}
	@Override
	public void mouseReleased(MouseEvent e)	{
		//Toggle AA, reset drag
		if(AAon) {canvas.enableAA(); model.update();}
		navController.updateNavList();
		model.pathFinder().drag(false, false);
		dragStart = false;
		dragEnd = false;
		dragPoint = false;
		draggingPOI = null;
	}

	/**
	 * Handles the ActionEvent of the Swing timers
	 * @param e The ActionEvent from either of the timers
     */
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == aaTimer) {
			//Responds to the delayed AA during zoom
			if(AAon) {canvas.enableAA();}
			model.update();
			aaTimer.stop();
		} else if(e.getSource() == mouseMoveTimer) {
			//If the mouse has not been moved for the amount of time specified by mouseMoveTimer the closest road
			//is shown.
			Road road = model.closestRoad(currentModelMousePosition);
			String roadName;
			if(road != null) {
				roadName = road.name();
			} else {
				mouseMoveTimer.stop();
				return;
			}
			float distToRoad = (float) model.closestRoad(currentModelMousePosition).distTo(currentModelMousePosition) * Model.METER_CONVERSION;
			if (roadName != null && !roadName.equals("") && canvas.detailLevel() >= 8 && distToRoad < 250) {
				canvas.infoBox(currentMousePosition, roadName);
				closestRoadDrawn = true;
			} else if (canvas.detailLevel() >= 8 && distToRoad < 250) {
				StringBuilder sb = new StringBuilder("Unnamed road. \n");
				switch(road.access()) {
					case ALLALLOWED:
						sb.append("All vehicles allowed.");
						break;
					case ONLYBIKE:
						sb.append("Only bikes allowed.");
						break;
					case ONLYCARS:
						sb.append("Only cars allowed.");
						break;
					case NONEALLOWED:
						sb.append("No vehicles allowed.");
						break;
				}
				canvas.infoBox(currentMousePosition, sb.toString());
				closestRoadDrawn = true;
			}
			mouseMoveTimer.stop();
		}
	}

	/**
	 * Turn off Anti-Aliasing to enable better frame-rate when the user pans frequently.
	 */
	public void keyPressed(KeyEvent e) {if(AAon) {canvas.disableAA();} }
	
	/**
	 * Turn on Anti-Aliasing (if enabled) to draw the map more precise.
	 */
	public void keyReleased(KeyEvent e) {if(AAon) {canvas.enableAA(); model.update();} }

	/**
	 * Handles shortcuts for the program
	 * @param e The KeyEvent corresponding to the typed key
     */
	public void keyTyped(KeyEvent e) {
		model.highlight(null);
		if (!addPOIMode) {
			switch (e.getKeyChar()) {
				case 'w':
					//Move up
					pan(0, 15);
					break;
				case 'a':
					//Move left
					pan(15, 0);
					break;
				case 'd':
					//Move right
					pan(-15, 0);
					break;
				case 's':
					//Move down
					pan(0, -15);
					break;
				case 'r':
					//Reset the view
					resetView();
					break;
				case 'o':
					//Toggle Anti-Aliasing
					if (AAon) {
						canvas.disableAA();
					} else {
						canvas.enableAA();
					}
					AAon = !AAon;
					break;
				case 'i':
					//Toggle the debug tool to show how buildings are compressed
					canvas.toggleCompressionTest();
					break;
				case 'h':
					//Show the help-screen
					if(navController == null) {navController = Main.navController();}
					navController.toggleHelp();
					break;
				case 'q':
					//Rotate left
					anchor = canvas.inverse(canvas.getWidth() / 2, canvas.getHeight() / 2);
					rotate(true, anchor);
					break;
				case 'e':
					//Rotate right
					anchor = canvas.inverse(canvas.getWidth() / 2, canvas.getHeight() / 2);
					rotate(false, anchor);
					break;
				case 't':
					//Turn debug tool for the DataTree on and off.
					canvas.toggleTreeTest();
					updateClip();
					break;
				case 'y':
					//Toggle the FPS counter for the map
					canvas.toggleFPSCounter();
					break;
				case 'u':
					//Toggle alpha
					alphaOn = !alphaOn;
					break;
				case 'p': 
					//Toggle openGL
					String status;
					if(canvas.toggleOpenGL()){
						status = "enabled";
					} else {
						status = "disabled";
					}
					JOptionPane.showMessageDialog(null, "OpenGl has been " + status, "OpenGL toggled", JOptionPane.OK_OPTION);
					break;
				case 'g':
					//Zoom to the middle of the map
					Point2D point = new Point2D.Float((model.minLon() + model.maxLon()) / 2, (model.minLat() + model.maxLat()) / 2);
					zoomToPoint(point, 250);
					break;
				default:
					break;
			}
		}
	}

	/**
	 * Returns the MapCanvas belonging to the controller
	 * @return The used MapCanvas
     */
	public MapCanvas mapCanvas() { return canvas; }

	/**
	 * Returns the current Config used by the MapCanvas
	 * @return The Config-object belonging to the MapCanvas
     */
	public Config config() { return canvas.config(); }

	/**
	 * Changes the Config-objec belonging to the MapCanvas
	 * @param config The desired Config
     */
	public void config(Config config) { canvas.config(config); }

	/**
	 * Enables or diables the searchbox of the view
	 * @param b Whether or not to enable the searchbox
     */
	public void showSearchBox(boolean b) {
		canvas.showSearchBox(b);
	}
}