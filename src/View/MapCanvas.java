package View;

import Controller.Main;
import Model.Model;
import Model.Config;
import Model.PolygonApprox;
import Model.POI;
import Model.Road;
import enums.ConfigType;
import enums.POIType;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import javax.swing.JComponent;

/**
 * The JComponent used to draw the map on.
 */
public class MapCanvas extends JComponent implements Observer{
	private static final long serialVersionUID = 16052016L;
	private AffineTransform trans = new AffineTransform();
	private Model model;
	private Config config;
	private boolean AA, compressionTest, treeTest, searchBox, openGL, showFPS;
	private int detailLevel, scale;
	private int buildingA, farmPlainA, grassA;
	private Font overlayFont = new Font("Segoe UI Semilight", Font.PLAIN, 14);
	private Point2D addingPoint, searchPointFirst, searchPointSecond;
	private String searchTxtFirst, searchTxtSecond;
	private long time = 0; // for Fps counter
	private Rectangle2D viewport;

	/**
	 * Initializes the MapCanvas with a given size and using the given Config object
	 * @param x The width of the canvas in pixels
	 * @param y The height of the canvas in pixels
     * @param config The Config object connected to the canvas
     */
	public MapCanvas(int x, int y, Config config) {
		setSize(x, y);
		AA = true;
		openGL = false;
		showFPS = false;
		model = Main.model();
		model.addObserver(this);
		this.config = config;
		IconPack.loadImages();
	}

	/**
	 * Initializes a default MapCanvas.
	 * @param x
	 * @param y
     */
	public MapCanvas(int x, int y){
		this(x, y, new Config("Config"));
	}

	/**
	 * Toggles OpenGL (never used).
	 * @return Whether or not OpenGL is activated now
     */
	public boolean toggleOpenGL() {
		openGL = !openGL;
		if(openGL) { System.setProperty("sun.java2d.opengl", "True"); }
		else { System.setProperty("sun.java2d.opengl", "False"); }
		return openGL;
	}

	/**
	 * Toggles whether or not the MapCanvas should use the entire screen of a test-view.
	 */
	public void toggleTreeTest(){treeTest = !treeTest;}

	/**
	 * Toggles whether or not buildings should be painted in random colors to show compression
	 */
	public void toggleCompressionTest(){compressionTest = !compressionTest;}

	/**
	 * Enables antialiasing for a cleaner view.
	 */
	public void enableAA() {AA = true;}

	/**
	 * Disables antialiasing for a faster view.
	 */
	public void disableAA() {AA = false;}

	/**
	 * Sets the level of details to be showed.
	 * @param n An integer describing how detailed the map should be drawn.
     */
	public void detailLevel(int n) {detailLevel = n;}

	/**
	 * Returns the current detail level.
	 * @return int An integer describing how detailed the map is drawn.
     */
	public int detailLevel() {return detailLevel;}

	/**
	 * Sets the number to be shown at the scale in the corner of the screen.
	 * @param zoomLevel The current amount of meters to be shown at the scale.
     */
	public void scale(int zoomLevel) {scale = zoomLevel;}

	/**
	 * Replaces the Config used by the MapCanvas.
	 * @param newConfig The new Config to be used.
     */
	public void config(Config newConfig) { config = newConfig; }
	/**
	 * Returns the current Config used by the MapCanvas.
	 * @return Config The current Config-object.
     */
	public Config config() {return config;}


	/**
	 * Enables the box, which depicts the result of a search.
	 * @param resultTxt
	 * @param location
     */
	public void enableSearchBox(String resultTxt, Point2D location) {
		searchTxtFirst = resultTxt;
		searchPointFirst = location;
		searchTxtSecond = null;
		searchPointSecond = null;
		if(searchPointFirst == null) {
			searchBox = false;
		} else {
			searchBox = true;
		}
	}

	/**
	 * Enables two boxes, which depicts the result of a route search.
	 * @param firstTxt
	 * @param firstLocation
	 * @param secondTxt
	 * @param secondLocation
     */
	public void enableSearchBox(String firstTxt, Point2D firstLocation, String secondTxt, Point2D secondLocation) {
		searchTxtFirst = firstTxt;
		searchPointFirst = firstLocation;
		searchTxtSecond = secondTxt;
		searchPointSecond = secondLocation;
		if(searchPointFirst == null || searchPointSecond == null) {
			searchBox = false;
		} else {
			searchBox = true;
		}
	}

	/**
	 * Disables search box.
	 */
	public void disableSearchBox() {
		searchTxtFirst = null;
		searchPointFirst = null;
		searchTxtSecond = null;
		searchPointSecond = null;
		searchBox = false;
	}

	/**
	 * Resets the AffineTransform used by the MapCanvas.
	 */
	public void resetTransform()
	{
		trans = new AffineTransform();
	}

	/**
	 * Translates a Point2D in model coordinates into a Point2D in view coordinates.
	 * @param p The Point2D in model coordinates.
	 * @return Point2D The corresponding Point2D in view coordinates.
	 */
	public Point2D translatePointToViewCoords(Point2D p) {
		Point2D returnPoint = new Point2D.Float(0,0);
		trans.transform(p, returnPoint);
		return returnPoint;
	}

	/**
	 * Translates a point in view coordinates into a Point2D in model coordinates using a custom AffineTransform.
	 * @param at The AffineTransform used to transform the point.
	 * @param dx The x-coordinates for the point.
	 * @param dy the y-coordinates for the point.
     * @return Point2D The Point2D in model coordinates.
     */
	public Point2D inverse(AffineTransform at, double dx, double dy){
		try {
			return at.inverseTransform(new Point2D.Float((float)dx,  (float)dy), null);
		} catch (NoninvertibleTransformException e) {
			throw new RuntimeException("Inverse fail");
		}
	}

	/**
	 * Translates a point in view coordinates into a Point2D in model coordinates using the current AffineTransform.
	 * @param dx The x-coordinate of the point.
	 * @param dy The y-coordinate of the point.
     * @return Point2D The point in model coordinates.
     */
	public Point2D inverse(double dx, double dy) {
		return inverse(trans, dx, dy);
	}

	/**
	 * Returns the bounds of the canvas in Model coordinates using a custom AffineTransform and an offset in x and y.
	 * @param at The AffineTransform used to find the bounds.
	 * @param offsetX An optional offset in the x-coordinates.
	 * @param offsetY An optional offset in the y-coordinates.
     * @return Rectangle2D The bounds of the view.
     */
	private Rectangle2D getViewPort(AffineTransform at, double offsetX, double offsetY)
	{
		Point2D p1 = inverse(at, 0 - offsetX, 0 - offsetY);
		Point2D p2 = inverse(at, getWidth() - offsetX, 0 - offsetY);
		Point2D p3 = inverse(at, 0 - offsetX, getHeight() - offsetY);
		Point2D p4 = inverse(at, getWidth() - offsetX, getHeight() - offsetY);
		double minX = Math.min(Math.min(p1.getX(),p2.getX()), Math.min(p3.getX(),p4.getX()));
		double maxX = Math.max(Math.max(p1.getX(),p2.getX()), Math.max(p3.getX(),p4.getX()));
		double minY = Math.min(Math.min(p1.getY(),p2.getY()), Math.min(p3.getY(),p4.getY()));
		double maxY = Math.max(Math.max(p1.getY(),p2.getY()), Math.max(p3.getY(),p4.getY()));

		Rectangle2D viewPort;
		if(treeTest) {
			viewPort = new Rectangle2D.Double(minX + (maxX - minX)/4, minY + (maxY - minY)/4 , (maxX - minX)/2, (maxY - minY)/2);
		} else {
			viewPort = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
		}
		return viewPort;
	}

	/**
	 * Returns the bounds of the MapCanvas in Model coordinates with a custom offset.
	 * @param offsetX An optional offset in the x-coordinates.
	 * @param offsetY An optional offset in the y-coordinates.
	 * @return Rectangle2D The bounds of the view.
     */
	public Rectangle2D getViewPort(double offsetX, double offsetY)
	{
		return getViewPort(trans, offsetX, offsetY);
	}

	/**
	 * Return the bounds of the current view in model coordinates.
	 * @return Rectangle2D The bounds of the view in model coordinates.
     */
	public Rectangle2D getViewPort(){
		return getViewPort(trans, 0, 0);
	}

	/**
	 * Returns a potential rotated viewport.
	 * @param amount The amount to rotate.
	 * @param anchor The anchor that is rotated around.
     * @return Rectangle2D The bounds of the view, if the screen is rotated the desired amount.
     */
	public Rectangle2D getRotateViewPort(double amount, Point2D anchor){
		AffineTransform nextAT = new AffineTransform(trans);
		nextAT.rotate(amount, anchor.getX(), anchor.getY());
		return getViewPort(nextAT, 0, 0);
	}

	/**
	 * Pans the view the desired amount.
	 * @param dx The x-value to be panned.
	 * @param dy The y-value to be panned.
     */
	public void pan(double dx, double dy){
		trans.preConcatenate(AffineTransform.getTranslateInstance(dx, dy));
	}

	/**
	 * Zooms the desired amount around an anchor point.
	 * @param zoomFactor The amount to be zoomed.
	 * @param dx The x-coordinate of the anchor point.
	 * @param dy The y-coordinate of the anchor point.
     */
	public void zoom(double zoomFactor, double dx, double dy){
		pan(-dx, -dy);
		trans.preConcatenate(AffineTransform.getScaleInstance(zoomFactor, zoomFactor));
		pan(dx, dy);
	}

	/**
	 * Rotates the MapCanvas.
	 * @param amount The amount to rotate.
	 * @param anchor The anchor point to rotate around.
     */
	public void rotate(double amount, Point2D anchor)
	{
		trans.rotate(amount, anchor.getX(), anchor.getY());
	}

	/**
	 * Updates the MapCanvas when an Observable is changed.
	 * @param obs
	 * @param obj
     */
	public void update(Observable obs, Object obj){
		repaint();
	}

	/**
	 * Updates the temporary point for where the POI should be added.
	 * @param point The Point for the POI.
     */
	public void addingPoint(Point2D point){
		addingPoint = point;
	}

	/**
	 * Toggles whether or not an FPSCounter is shown in the top left corner.
     */
	public void toggleFPSCounter(){
		showFPS = !showFPS;
	}

	/**
	 * Used to make special stroke for cycle- and walkways.
	 * @return
     */
	private BasicStroke cycleAndWalkwayStroke() {
		float[] dasharray = {0.000025f,0.00001f,0.00001f,0.00001f};
		return new BasicStroke(0.00001f,
						BasicStroke.CAP_BUTT,
						BasicStroke.JOIN_ROUND,
						1.0f, dasharray, 0.0f);
	}

	/**
	 * Used to make special stroke for railways (not used).
	 * @return
     */
	private BasicStroke railwayStroke() {
		float[] dasharray = {0.000045f,0.000045f,0.000045f,0.000045f};
		return new BasicStroke(0.00002f,
						BasicStroke.CAP_BUTT,
						BasicStroke.JOIN_ROUND,
						1.0f, dasharray, 0.0f);
	}

	/**
	 * Used to determine the stroke of different paths.
	 * @param detail The given detail level.
	 * @return
     */
	private BasicStroke pathStroke(int detail) {
		switch (detail) {
			case 1:
				return new BasicStroke(0.00060f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			case 2:
				return new BasicStroke(0.00045f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			case 3:
				return new BasicStroke(0.00030f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			case 4:
				return new BasicStroke(0.00025f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			case 5:
				return new BasicStroke(0.00020f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			case 6:
				return new BasicStroke(0.00016f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			case 7:
				return new BasicStroke(0.00013f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			case 8:
				return new BasicStroke(0.00010f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			case 9:
				return new BasicStroke(0.00008f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			case 10:
				return new BasicStroke(0.00006f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			case 11:
				return new BasicStroke(0.00004f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

		}
		return new BasicStroke(0.00004f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	}

	/**
	 * Used to determine stroke of road.
	 * @return
     */
	private BasicStroke roadStroke() {
		return new BasicStroke((float) 1.8e-5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	}

	/**
	 * Set the current alpha value for buildings.
	 * @param ba The alpha value for buildings as an int.
     */
	public void buildingA(int ba){
		if(ba < 0){
			ba = 0;
		} else if(ba > 255){
			ba = 255;
		}
		buildingA = ba;
	}
	/**
	 * Set the current alpha value for grass.
	 * @param ga The alpha value for grass as an int.
	 */
	public void grassA(int ga) {
		if(ga < 0){
			ga = 0;
		} else if(ga > 255){
			ga = 255;
		}
		grassA = ga;
	}
	/**
	 * Set the current alpha value for farmland and plains.
	 * @param fpa The alpha value for farmland and plains as an int.
	 */
	public void farmPlainA(int fpa) {
		if(fpa < 0){
			fpa = 0;
		} else if(fpa > 255){
			fpa = 255;
		}
		farmPlainA = fpa;
	}

	/**
	 * Sets the current viewport for the MapCanvas.
	 * @param rr The new viewport.
     */
	public void viewport(Rectangle2D rr){
		viewport = rr;
	}

	/**
	 * Paints all the objects in the current viewport.
	 * @param gg The Graphics needed to draw this.
     */
	public void paint(Graphics gg){
		requestFocus();
		float requiredSize = (float) (35/Math.abs(trans.getDeterminant()));
		time = 9*time - System.nanoTime();
		Graphics2D g = (Graphics2D) gg;
		if(AA) {g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);}
		g.setStroke(roadStroke());
		if(model.dataCoast(viewport).size() > 0){
			g.setColor(config.color(ConfigType.WATER));
			g.fill(new Rectangle2D.Double(-2, -2, getWidth() + 4, getHeight() + 4));
			g.setTransform(trans);
			g.setColor(config.color(ConfigType.BACKGROUND));
			for (Shape[] sa: model.dataCoast(viewport)){
				for(Shape s: sa){
					if(insideViewport(s.getBounds2D())){
						g.fill(s);
					}
				}
			}
		} else {
			g.setColor(config.color(ConfigType.BACKGROUND));
			g.fill(new Rectangle2D.Double(-2, -2, getWidth() + 4, getHeight() + 4));
			g.setTransform(trans);
		}

		//Drawing sand areas.
		if(config.visible(ConfigType.SAND) && detailLevel >= 6){
			g.setColor(config.color(ConfigType.SAND));
			for (Shape[] sa: model.dataSand(viewport)){
				for(Shape s: sa){
					if(insideViewport(s.getBounds2D())){
						g.fill(s);
					}
				}
			}
		}

		//Drawing farmland areas.
		if(config.visible(ConfigType.FARMLAND) && detailLevel >= 4) {
			if(detailLevel >= 5) {
				g.setColor(config.color(ConfigType.FARMLAND));
			} else {
				Color co = config.color(ConfigType.FARMLAND);
				g.setColor(new Color(co.getRed(), co.getGreen(), co.getBlue(), farmPlainA));
			}
			for(Shape[] sa : model.dataFarm(viewport)){
				for(Shape s: sa){
					if(insideViewport(s.getBounds2D())){
						g.fill(s);
					}
				}
			}
		}

		//Drawing plain areas.
		if(config.visible(ConfigType.PLAIN)  && detailLevel >= 4) {
			if(detailLevel >= 5) {
				g.setColor(config.color(ConfigType.PLAIN));
			} else {
				Color co = config.color(ConfigType.PLAIN);
				g.setColor(new Color(co.getRed(), co.getGreen(), co.getBlue(), farmPlainA));
			}
			for(Shape[] sa : model.dataPlain(viewport)){
				for(Shape s: sa){
					if(insideViewport(s.getBounds2D())){
						g.fill(s);
					}
				}
			}
		}

		//Drawing residential areas.
		if(config.visible(ConfigType.RESIDENTIAL) && detailLevel >= 2) {
			g.setColor(config.color(ConfigType.RESIDENTIAL));
			for(Shape[] sa: model.dataResidential(viewport)) {
				for(Shape s: sa){
					if(insideViewport(s.getBounds2D())){
						g.fill(s);
					}
				}
			}
		}

		//Drawing industrial areas.
		if(config.visible(ConfigType.INDUSTRIAL) && detailLevel >= 2) {
			if(detailLevel > 3) {
				g.setColor(config.color(ConfigType.INDUSTRIAL));
			} else {
				g.setColor(config.color(ConfigType.RESIDENTIAL));
			}
			for(Shape[] sa : model.dataIndustrial(viewport)){
				for(Shape s: sa){
					if(insideViewport(s.getBounds2D())){
						g.fill(s);
					}
				}
			}
		}

		//Drawing grass areas.
		if(config.visible(ConfigType.GRASS) && detailLevel >= 7) {
			if(detailLevel >= 8){
				g.setColor(config.color(ConfigType.GRASS));
			} else {
				Color co = config.color(ConfigType.GRASS);
				g.setColor(new Color(co.getRed(), co.getGreen(), co.getBlue(), grassA));
			}
			for(Shape[] sa : model.dataGrass(viewport)){
				for(Shape s: sa){
					if(insideViewport(s.getBounds2D())){
						g.fill(s);
					}
				}
			}
		}

		//Drawing forests.
		if(config.visible(ConfigType.FOREST)  && detailLevel >= 2){
			g.setColor(config.color(ConfigType.FOREST));
			for(PolygonApprox[] paa : model.dataForest(viewport)){
				for(PolygonApprox pa: paa){
					if(pa.getSize() >= requiredSize){
						if(insideViewport(pa.getBounds2D())){
							g.fill(pa);
						}
					} else {
						break;
					}
				}
			}
		}

		//Drawing parking areas.
		if(config.visible(ConfigType.PARKING) && detailLevel >= 12) {
			g.setColor(config.color(ConfigType.PARKING));
			for(Shape[] sa : model.dataParking(viewport)){
				for(Shape s: sa){
					if(insideViewport(s.getBounds2D())){
						g.fill(s);
					}
				}
			}
		}

		//Drawing buildings.
		if(config.visible(ConfigType.BUILDING) && detailLevel >= 11){
			g.setColor(config.color(ConfigType.BUILDING));
			if(compressionTest) {
				Random r = new Random();
				for (Shape[] sa : model.dataBuilding(viewport)) {
					for (Shape s : sa) {
						if (insideViewport(s.getBounds2D())) {
							g.setColor(new Color(r.nextInt(255),r.nextInt(255),r.nextInt(255)));
							g.fill(s);
						}
					}
				}
			} else {
				for (Shape[] sa : model.dataBuilding(viewport)) {
					for (Shape s : sa) {
						if (insideViewport(s.getBounds2D())) {
							g.fill(s);
						}
					}
				}
			}
		}

		// Draw water (lakes and other water areas, not oceans).
		if(config.visible(ConfigType.WATER)) {
			g.setColor(config.color(ConfigType.WATER));
			for(PolygonApprox[] paa : model.dataWater(viewport)){
				for(PolygonApprox pa: paa) {
					if (/*pa.getBounds2D().intersects(viewport) && */pa.getSize() >= requiredSize) {
						if(insideViewport(pa.getBounds2D())){
							g.fill(pa);
						}
					} else {
						break;
					}
				}
			}
			//Drawing waterways.
			if(detailLevel >= 4) {
				for(Shape[] sa : model.dataWaterway(viewport)){
					for(Shape s: sa){
						if(insideViewport(s.getBounds2D())){
							g.draw(s);
						}
					}
				}
			}
		}

		//Drawing park walls (mostly to encapsulate Copenhagen zoo.
		if(detailLevel >= 12){
			g.setColor(new Color(157, 43, 5));
			for (Shape s : model.parkWall()) {
				if(insideViewport(s.getBounds2D())){
					g.draw(s);
				}
			}
		}

		//Draw railways.
		if(config.visible(ConfigType.RAILWAY) && detailLevel >= 3) {
			g.setColor(config.color(ConfigType.RAILWAY));
			g.setStroke(new BasicStroke(0.000025f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			for(Shape[] sa : model.dataRailway(viewport)){
				for(Shape s: sa){
					if(insideViewport(s.getBounds2D())){
						g.draw(s);
					}
				}
			}
		}

		//Draw walkways.
		if(config.visible(ConfigType.WALKWAY) && detailLevel >= 13) {
			g.setColor(config.color(ConfigType.WALKWAY));
			g.setStroke(cycleAndWalkwayStroke());
			for(Shape[] sa : model.dataWalk(viewport)) {
				for(Shape s : sa){
					if(insideViewport(s.getBounds2D())){
						g.draw(s);
					}
				}
			}
		}

		//Draw roads and set stroke depending on type.
		if(config.visible(ConfigType.SMALLROADS)) {
			if (detailLevel >= 10) {
				g.setStroke(new BasicStroke(0.000025f));
				g.setColor(config.color(ConfigType.SMALLROADS));
				for (Shape[] sa : model.dataRoad_lvl5(viewport)) {
					for (Shape s : sa) {
						if(insideViewport(s.getBounds2D())){
							g.draw(s);
						}
					}
				}
			}
			if (detailLevel >= 9) {
				g.setStroke(new BasicStroke(0.000025f));
				g.setColor(config.color(ConfigType.SMALLROADS));
				for (Shape[] sa : model.dataRoad_lvl4(viewport)) {
					for (Shape s: sa) {
						if(insideViewport(s.getBounds2D())){
							g.draw(s);
						}
					}
				}
			}
		}

		if(config.visible(ConfigType.MOTORWAY)) {
			g.setStroke(new BasicStroke(0.00004f));
			g.setColor(config.color(ConfigType.MOTORWAY));
			for (Shape[] sa : model.dataRoad_lvl1(viewport)) {
				for (Shape s: sa) {
					if(insideViewport(s.getBounds2D())){
						g.draw(s);
					}
				}
			}
		}

		if(config.visible(ConfigType.BIGROADS)) {
			if (detailLevel >= 4) {
				g.setStroke(new BasicStroke(0.00003f));
				g.setColor(config.color(ConfigType.BIGROADS));
				for (Shape[] sa : model.dataRoad_lvl3(viewport)) {
					for (Shape s: sa) {
						if(insideViewport(s.getBounds2D())){
							g.draw(s);
						}
					}
				}
			}

			g.setStroke(new BasicStroke(0.000035f));
			g.setColor(config.color(ConfigType.BIGROADS));
			for (Shape[] sa : model.dataRoad_lvl2(viewport)) {
				for (Shape s: sa) {
					if(insideViewport(s.getBounds2D())){
						g.draw(s);
					}
				}
			}
		}

		//Drawing shapes for airports.
		if(config.visible(ConfigType.AIRWAY) && detailLevel >= 9) {
			g.setColor(config.color(ConfigType.AIRWAY));
			for(Shape s: model.airport()){
				if(insideViewport(s.getBounds2D())){
					g.draw(s);
				}
			}
		}

		// Draw cycleways.
		if(config.visible(ConfigType.CYCLE) && detailLevel >= 13) {
			g.setColor(config.color(ConfigType.CYCLE));
			g.setStroke(cycleAndWalkwayStroke());
			for(Shape[] sa : model.dataCycle(viewport)) {
				for(Shape s: sa){
					if(insideViewport(s.getBounds2D())){
						g.draw(s);
					}
				}
			}
		}

		// Draw navigation route.
		Road[] path = model.path();
		g.setColor(config.color(ConfigType.ROUTE));
		if(path != null) {
			for (int i = 0; i < path.length; i++) {
				if ((i == 0 || i == path.length - 1) && path[i].name().contains("[WALKWAY]")) {
					g.setStroke(cycleAndWalkwayStroke());
					g.draw(path[i]);
				} else {
					//Drawing the current path found by PathFinder
					g.setStroke(pathStroke(detailLevel));
					g.draw(path[i]);
				}
			}
		}

		//The last things should be drawn without using the transform
		g.setTransform(new AffineTransform());

		//Drawing highlight
		if(model.highlight() != null) {
			float[] dia = {25f,20f,15f};

			g.setColor(Color.RED);
			g.fill(new Ellipse2D.Float((float) model.highlight().getX() - (dia[0]/2), (float) model.highlight().getY() - (dia[0]/2), dia[0], dia[0]));
			g.setColor(Color.WHITE);
			g.fill(new Ellipse2D.Float((float) model.highlight().getX() - (dia[1]/2), (float) model.highlight().getY() - (dia[1]/2), dia[1], dia[1]));
			g.setColor(Color.RED);
			g.fill(new Ellipse2D.Float((float) model.highlight().getX() - (dia[2]/2), (float) model.highlight().getY() - (dia[2]/2), dia[2], dia[2]));
		}

		//Draw POI's
		if(detailLevel >= 14){
			for (POI p : model.POI()) {
				Point2D pp = trans.transform(p, new Point2D.Float());
				int x = (int) pp.getX();
				int y = (int) pp.getY();
				//3 % of total = security margin. Be sure to draw every POI inside canvasview.
				int xMargin = (int) (getWidth() * 0.03);
				int yMargin = (int) (getHeight() * 0.03);
				if ((0 - xMargin) < x && x < (getWidth() + xMargin) && (0 - yMargin) < y && y < (getHeight() + yMargin)) {
					g.drawImage(IconPack.POIIcon(p.type()), ((int) pp.getX()) - 16, ((int) pp.getY()) - 32, 32, 32, null);
				}
			}
		}
		for(POI p : model.userPOI()) {
			Point2D pp = trans.transform(p, new Point2D.Float());
			g.drawImage(IconPack.userPOIIcon(p.type()), (int) pp.getX() - 16, (int) pp.getY() - 16, 32, 32, null);
		}
		if(addingPoint != null){
			g.drawImage(IconPack.userPOIIcon(POIType.STAR), (int) addingPoint.getX() -16, (int) addingPoint.getY() -16, 32, 32, null);
		}

		//Draw zoom-scale line for 100px with text. 
		g.setFont(overlayFont);
		Point2D start = new Point2D.Float(getWidth() - 120, getHeight()-30);
		Point2D end = new Point2D.Float(getWidth() - 20, getHeight()-30);
		Line2D line = new Line2D.Float(start,end);
		Rectangle2D bg = new Rectangle2D.Float(getWidth()-130, getHeight()-40, 120, g.getFontMetrics().getHeight()+ 20);
		g.setColor(new Color(255,255,255,140));
		g.fill(bg);
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(4f));
		g.draw(line);
		g.setColor(Color.WHITE);
		float[] dash = {5.0f};
		g.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND,	BasicStroke.JOIN_ROUND,	1.0f, dash, 0.0f));
		g.draw(line);
		g.setColor(Color.BLACK);
		String overlayTxt = "~ " + scale + "m";
		g.drawString(overlayTxt, getWidth() - 50 - g.getFontMetrics().stringWidth(overlayTxt), getHeight() - 10);

		time += System.nanoTime();
		time /= 10;
		if(showFPS) {
			//Draw fps counter box
			String fps = String.format("FPS: %.1f", 1e9 / time);
			Rectangle2D fpsBG = new Rectangle2D.Float(15, 15, g.getFontMetrics().stringWidth(fps) + 10, g.getFontMetrics().getAscent() + 5);
			g.setColor(new Color(255, 255, 255, 140));
			g.fill(fpsBG);
			g.setColor(Color.BLACK);
			g.drawString(fps, 20, g.getFontMetrics().getAscent() + 15);
		}

		//Drawing the testviewport for debugging the datatrees.
		if(treeTest){
			g.setColor(Color.BLACK);
			Path2D box = new Path2D.Float(Path2D.WIND_EVEN_ODD);
			box.moveTo(getWidth()/4, getHeight()/4);
			box.lineTo((3*getWidth())/4, getHeight()/4);
			box.lineTo((3*getWidth())/4, (3*getHeight())/4);
			box.lineTo(getWidth()/4, (3*getHeight())/4);
			box.lineTo(getWidth()/4, getHeight()/4);

			box.moveTo(getWidth()/4-5, getHeight()/4-5);
			box.lineTo((3*getWidth())/4+5, getHeight()/4-5);
			box.lineTo((3*getWidth())/4+5, (3*getHeight())/4+5);
			box.lineTo(getWidth()/4-5, (3*getHeight())/4+5);
			box.lineTo(getWidth()/4-5, getHeight()/4-5);

			g.fill(box);
		}

		//Draw searchBox if searchresults have been handed over
		if(searchBox && searchPointFirst != null) {
			Point2D.Float screenCords = (Point2D.Float) trans.transform(searchPointFirst, new Point2D.Float());
			Font font = overlayFont;
			FontMetrics metrics = g.getFontMetrics(font);
			int textWidth = metrics.stringWidth(searchTxtFirst);
			int textHeight = metrics.getHeight();

			int x =  (int) (screenCords.getX() - (textWidth/2));
			int y =  (int) (screenCords.getY() - 20);

			Rectangle backgroundBox = new Rectangle(x-5, y-textHeight, textWidth+10, textHeight+10);
			g.setStroke(new BasicStroke(1f));
			g.setColor(NavScreen.fromBoxColor());
			g.fill(backgroundBox);
			g.setStroke(new BasicStroke(3f));
			g.drawLine(x+(textWidth/2), y+10, x+(textWidth/2), y+20);
			g.setColor(Color.WHITE);
			g.drawString(searchTxtFirst, x, y);
			if(searchTxtSecond != null) {
				screenCords = (Point2D.Float) trans.transform(searchPointSecond, new Point2D.Float());
				font = overlayFont;
				metrics = g.getFontMetrics(font);
				textWidth = metrics.stringWidth(searchTxtSecond);
				textHeight = metrics.getHeight();

				x =  (int) (screenCords.getX() - (textWidth/2));
				y =  (int) (screenCords.getY() - 20);

				backgroundBox = new Rectangle(x-5, y-textHeight, textWidth+10, textHeight+10);
				g.setStroke(new BasicStroke(1f));
				g.setColor(NavScreen.toBoxColor());
				g.fill(backgroundBox);
				g.setStroke(new BasicStroke(3f));
				g.drawLine(x+(textWidth/2), y+10, x+(textWidth/2), y+20);
				g.setColor(Color.WHITE);
				g.drawString(searchTxtSecond, x, y);
			}
		}
	}

	/**
	 * Calculates whether a Rectangle2D is inside the viewport or not.
	 * @param drawable The bounds of a drawable object to be tested.
	 * @return boolean Whether or not the object should be drawn.
     */
	private boolean insideViewport(Rectangle2D drawable){
		if(drawable.getMinX() > viewport.getMaxX() || drawable.getMaxX() < viewport.getMinX() || drawable.getMinY() > viewport.getMaxY() || drawable.getMaxY() < viewport.getMinY()){
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Sets the boolean searchBox.
	 * @param b
     */
	public void showSearchBox(boolean b) {
		searchBox = b;
	}

	/**
	 * Draws a box with text.
	 * @param position View coordinates expected.
	 * @param infoText
     */
	public void infoBox(Point2D position, String infoText) {
		if(!(infoText == null || infoText.equals(""))) {
			int x = (int) position.getX();
			int y = (int) position.getY() - 20;
			Graphics2D g = (Graphics2D) this.getGraphics();
			Font font = overlayFont;
			FontMetrics metrics = g.getFontMetrics(font);
			int textWidth = metrics.stringWidth(infoText);
			int textHeight = metrics.getHeight();
			Rectangle backgroundBox = new Rectangle(x - 6, y - textHeight + 1, textWidth + 4, textHeight + 4);

			g.setColor(new Color(112, 128, 144));
			g.fill(backgroundBox);
			g.setColor(Color.WHITE);
			g.draw(backgroundBox);
			g.drawString(infoText, x, y);
		}
	}
}