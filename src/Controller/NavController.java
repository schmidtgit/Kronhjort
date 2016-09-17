package Controller;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;	
import java.awt.geom.Point2D;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import Model.Address;
import Model.Config;
import Model.Model;
import Model.POI;
import Model.Road;
import Model.Vertex;
import Model.NoPathFoundException;
import View.NavScreen;
import View.NavigationList;
import View.HelpScreen;
import View.POIlist;
import View.AddressFinder;
import View.RoutePrinter;
import View.LoadingScreen;

/**
 * Controller for NavScreen.
 * Handles all input from NavScreen except for the settings part. 
 */
public class NavController {
	private MapController mapCtrl;
	private NavScreen frame; 
	private NavigationList navList;
	private boolean showingPath;  //If the pathfinder has been activated
	private Point2D toPoint, fromPoint;    //(address)Points based on searchbox input
	private JFileChooser fc; //Dialog for save/load
	private HelpScreen help;

	/**
	 * Sets up NavController and all needed classes.
	 * @param width Specifies initial width of the NavScreen.
	 * @param height Specifies initial height of the NavScreen.
     */
	public NavController(int width, int height) {
		mapCtrl = new MapController(this, (int)(width*0.6), (int)(height*0.6));
		SettingsController settingCtrl = new SettingsController(this);
		POIlist poi = new POIlist(mapCtrl);
		navList = new NavigationList(mapCtrl, this);
		frame = new NavScreen(width, height, this, poi, mapCtrl.mapCanvas(), navList, settingCtrl.settingsPane());
		settingCtrl.addBackCtrl(cardSwitch(frame.sideCardLayout(), frame.sidePanel()));
		fc = new JFileChooser();
	}

	/**
	 * Returns the current width of the NavScreen
	 * @return
     */
	public int screenWidth(){
		return frame.getWidth();
	}

	/**
	 * Returns the current height of the NavScreen
	 * @return
     */
	public int screenHeight(){
		return frame.getHeight();
	}

	/**
	 * Return the current location of the NavScreen
	 * @return
     */
	public Point screenLocation(){
		return frame.getLocationOnScreen();
	}

	/**
	 * Sets the location of the NavScreen
	 * @param location The desired location
     */
	public void screenLocation(Point location){
		frame.setLocation(location);
	}

	/**
	 * Disposes the current NavScreen
	 */
	public void dispose(){
		frame.dispose();
	}

	/**
	 * Enables auto-zoom for MapCanvas when window size changes.
	 * Relies on the old window size being saved within NavScreen.
	 * @return Listener that resizes the JFrame, NavScreen.
	 */
	public ComponentListener resizeCtrl() {
		ComponentAdapter listener = new ComponentAdapter() {
		    public void componentResized(ComponentEvent e) {
				if(frame == null) { return; }
		        double deltaW = (double) e.getComponent().getWidth() / (double) frame.oldX();
		        double deltaH = (double) e.getComponent().getHeight() / (double) frame.oldY();
		        mapCtrl.zoom(Math.max(deltaW, deltaH), 0, 0);
		        frame.oldX(e.getComponent().getWidth());
		        frame.oldY(e.getComponent().getHeight());
		    }
		};
		return listener;
	}

	/**
	 * Handles toggling of car/bike, shortest/fastest.
	 * The listener will update the path stored in model whenever selection is toggled.
	 * @return Listener for toggle buttons in navigation.
	 */
	public ActionListener toggleListener(){
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(frame.carSelected()) {
					frame.fastTglEnable(true);
				} else {
					frame.shortSetSelected(true);
					frame.fastTglEnable(false);
				}
				if(showingPath){
					mapCtrl.showSearchBox(true);
					Road[] path = new Road[0];
					//Retrieves points to navigate to/from.
					fromPoint = Main.model().pathFrom();
					toPoint = Main.model().pathTo();
					try {
						//Recalculate path with new settings
						path = Main.model().pathFinder().path(fromPoint, toPoint, frame.carSelected(), frame.fastSelected());
						Main.model().path(path);
						Main.model().update();
						navList.updateList();
					} catch (NoPathFoundException ex) {
						JOptionPane.showMessageDialog(null, "No path found.", "Navigation error", JOptionPane.ERROR_MESSAGE);
						Main.model().resetPath();
						navList.updateList();
						mapCtrl.showSearchBox(false);
					}
				}
			}
		};
		return listener;
	}

	/**
	 * Pan control for arrow-overlayButtons.
	 * Sends the command to MapController.
	 * @param direction Expects "up", "left", "down", "right" as a string.
	 * @return Pan-interpreter for JButton.
	 */
	public ActionListener panCtrl(String direction) {
		int panX = 0;
		int panY = 0;
		switch(direction.toLowerCase()) {
			case "up": 
				panY = 25;
				break;
			case "down":
				panY = -25;
				break;
			case "left":
				panX = 25;
				break;
			case "right":
				panX = -25;
				break;
		}
		final int x = panX;
		final int y = panY;  //Must be final for actionlistener to work
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mapCtrl.pan(x, y);
			}
		};
		return listener;
	}
	
	/**
	 * Zoom control for overlayButtons.
	 * @param in True to zoom in, false to zoom out.
	 * @return Zoom-control for JButton
	 */
	public ActionListener zoomCtrl(Boolean in) {
		double zoom = 0;
		if(in) {zoom = Math.pow(1.1, 3);}
		else {zoom = Math.pow(1.1, -3);}
		final double zoomAmount = zoom;
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mapCtrl.zoom(zoomAmount);
			}
		};
		return listener;
	}
	
	/**
	 * Rotate control for overlayButtons.
	 * @param right True to rotate right around, false for left around.
	 * @return Listener for rotate.
	 */
	public ActionListener rotateCtrl(Boolean right) {
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mapCtrl.rotate(!right);
			}
		};
		return listener;
	}
	
	/**
	 * Control for reset overlay button.
	 * @return Listener that resets canvas pan/zoom/rotate.
	 */
	public ActionListener resetCtrl() {
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mapCtrl.resetView();
			}
		};
		return listener;
	}
	
	/**
	 * ActionListener for issuing a search.
	 * @param fromBox The AddressFinder to fetch search text from
	 * @return Listener to handle search.
	 */
	public ActionListener searchBtnCtrl(AddressFinder fromBox) {
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Try to search
				if(fromBox.getPoint() != null) { performSearch(fromBox); }
				else { JOptionPane.showMessageDialog(fromBox, "No search results found."); }
			}
		};
		return listener;
	}

	/**
	 * Listener for issuing navigation search between two addresses.
	 * Uses two AddressFinder boxes and Model.PathFinder to create a navigation route.
	 * Updates view and model.
	 * @param fromBox AddressFinder with starting address text
	 * @param toBox AddressFinder with destination address text
	 * @return Listener to handle navigation
	 */
	public ActionListener findRouteBtnCtrl(AddressFinder fromBox, AddressFinder toBox) {
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Extract points in model space. 
				Point2D a = fromBox.getPoint();
				Point2D b = toBox.getPoint();
				if(a != null && b != null) {
					//If there were points, begin to find center between the two points. 
					if(fromBox.getPoint().getX() > toBox.getPoint().getX()) {
						Point2D tmp = a;
						a = b;
						b = tmp;
					}		
					Double deltaX = b.getX() - a.getX();
					Double deltaY = Math.abs(b.getY() - a.getY());
					Double coordX = a.getX() + (deltaX/2);
					Double coordY = a.getY();
					if(a.getY() < b.getY()) {
						coordY += deltaY/2; 
					} else {
						coordY -= deltaY/2;
					}
					//Zoom to the calculated center of the two points. And calculate route
					Point2D zoomPoint = new Point2D.Double(coordX, coordY);
					if(deltaX > deltaY) {
						performRoute(fromBox, toBox, zoomPoint, deltaX);
					} else {
						performRoute(fromBox, toBox, zoomPoint, deltaY);	
					}
				//Performs analysis of from and to fields and tries to come up with a proper failure dialog.
				} else {
					String msg = "";
					if(fromBox.toString().equals("") && toBox.toString().equals("")) {
						msg = "Both search fields are empty.\nPlease provide input.";
					} else if(a == null) {
						if(fromBox.toString().equals("")) {
							msg = "The upper search field is empty.\nPlease provide input.";
						} else {
							msg = "The input in the upper search field could not be resolved to a valid address.\n(" + fromBox.toString() + ")\nPlease check the input.";
						}
					} else if(b == null) {
						if(toBox.toString().equals("")) {
							msg = "The lower search field is empty.\nPlease provide input.";
						} else {
							msg = "The input in the lower search field could not be resolved to a valid address.\n(" + toBox.toString() + ")\nPlease check the input.";
						}
					}
					JOptionPane.showMessageDialog(fromBox, msg, "Address not found", JOptionPane.ERROR_MESSAGE);
				}
					
			}
		};
		return listener;
	}
	
	/**
	 * Issues a search based on the text of the given AddressFinder.
	 * @param fromBox AddressFinder to get text from.
	 */
	private void performSearch(AddressFinder fromBox) {
		if(fromBox.getPoint() instanceof Address) { //Try searching for address.
			Main.model().resetPath();
			navList.updateList();
			mapCtrl.showSearchBox(false);

			Address result = (Address) fromBox.getPoint();
			mapCtrl.zoomToPoint(result, 100);
			mapCtrl.showSearchBox(result.toString(), result);
		} else if(fromBox.getPoint() instanceof POI) { //Try searching for POI.
			Main.model().resetPath();
			navList.updateList();
			mapCtrl.showSearchBox(false);

			POI result = (POI) fromBox.getPoint();
			mapCtrl.zoomToPoint(result, 100);
			mapCtrl.showSearchBox(result.toString(), result);
		}
	}

	/**
	 * Calculates a route based on the two AddressFinders, and zooms to the route.
	 * @param fromBox Used to get the fromPoint.
	 * @param toBox Used to get the toPoint.
	 * @param zoom Used to zoom to the route.
	 * @param width Width of the zoom.
	 */
	private void performRoute(AddressFinder fromBox, AddressFinder toBox, Point2D zoom, double width) {
		int mapWidth = (int) (width * Model.METER_CONVERSION * 1.1);
		fromPoint = fromBox.getPoint();
		toPoint = toBox.getPoint();

		try {
			Road[] path = Main.model().pathFinder().path(fromPoint, toPoint, frame.carSelected(), frame.fastSelected());
			Main.model().path(path);
			Main.model().pathFrom(fromPoint);
			Main.model().pathTo(toPoint);
			Main.model().update();

			showingPath = true;
			mapCtrl.showSearchBox(true);
			navList.updateList();

			mapCtrl.showSearchBox(fromPoint.toString(), fromPoint, toPoint.toString(), toPoint);
			mapCtrl.zoomToPoint(zoom, mapWidth);

			if (showingPath && frame.printEnabled() == false) {
				frame.printBtnEnable(true);
			}
			if (frame.fastSelected() && !frame.carSelected()) {
				path = Main.model().pathFinder().path(fromPoint, toPoint, frame.carSelected(), frame.fastSelected());
				frame.shortSetSelected(true);
				frame.fastTglEnable(false);
				Main.model().path(path);
				Main.model().update();
			}
		} catch (NoPathFoundException e) {
			JOptionPane.showMessageDialog(null, "No path found.", "Navigation error", JOptionPane.ERROR_MESSAGE);
			Main.model().resetPath();
			navList.updateList();
			mapCtrl.showSearchBox(false);
		}
	}
	
	/**
	 * Listener to enable adding of Points of interest (POI).
	 * @param cl: CardLayout with POI card.
	 * @param parent: Container of the CardLayout.
	 * @return Listener to add POI.
	 */
	public ActionListener addCtrl(CardLayout cl, Container parent) {
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mapCtrl.addingMode(); //Propagate adding to MapController
				cl.first(parent); //Select POI-panel as visible in cardlayout
			}
		};
		return listener;
	}

	/**
	 * Creates a listener to toggle HelpScreen.
	 * @return listener to toggle HelpScreen.
     */
	public ActionListener helpCtrl() {
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) { toggleHelp(); }
		};
		return listener;
	}
	
	/**
	 * Toggles HelpScreen visibility.
	 */
	public void toggleHelp() {
		if(help == null) { help = new HelpScreen(); return; }
		help.setVisible(!help.isVisible());
	}
	
	/**
	 * Creates listener for save button in sidePanel.
	 * @return Listener to save current model.
	 */
	public ActionListener saveCtrl() {
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fc.setFileFilter(new FileNameExtensionFilter("Object(*.obj)", "obj"));
				fc.setDialogTitle("Select where to save file");
				if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					LoadingScreen ls = new LoadingScreen("Saving model object...");
					File file = fc.getSelectedFile();
					if(!file.getName().endsWith(".obj")) {
						file = new File(file + ".obj");
					}
					try {
						Main.model().save(file);
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(null, "Encountered an error while saving the file.\nTry again.", "Save error", JOptionPane.ERROR_MESSAGE);
					}
					ls.dispose();
				}
			}
		};
		return listener;
	}
	
	/**
	 * Creates listener for load button in sidePanel.
	 * @return Listener to restart with new model. 
	 */
	public ActionListener loadCtrl() {
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fc = new JFileChooser();
				fc.setFileFilter(new FileNameExtensionFilter("OSM-files (*.osm; *.zip; *.obj)", "osm", "obj", "zip"));
				fc.setDialogTitle("Select file to be loaded");
				if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					Main.loadNewModel(fc.getSelectedFile());
				}
			}
		};
		return listener;
	}
	
	/**
	 * Creates listener for print button in sidePanel.
	 * @return Listener for print button.
	 */
	public ActionListener printCtrl(AddressFinder fromBox, AddressFinder toBox) {
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(Main.model().path() == null || Main.model().path().length == 0) {
					JOptionPane.showMessageDialog(frame, "There is currently no route to print.\nSearch for a route before printing");
					return;
				}
				PrinterJob job = PrinterJob.getPrinterJob();
				RoutePrinter print = new RoutePrinter(navList.getDirections(), navList.estimateTime(), navList.estimateLength(), fromBox.toString(), toBox.toString());
				job.setPrintable(print);
				boolean ok = job.printDialog();
			    if (ok) {
			    	try {
			    		job.print();
			        } catch (PrinterException ex) {
						JOptionPane.showMessageDialog(null, "Printing failed.", "Print error", JOptionPane.ERROR_MESSAGE);
			        }
			    }
			}
		};
		return listener;
	}
	
	/**
	 * Swaps two AddressFinders.
	 * @param box1 The first AddressFinder
	 * @param box2 The second AddressFinder
	 * @return Listener that can swap AddressFinder content
	 */
	public ActionListener switchTxtCtrl(AddressFinder box1, AddressFinder box2) {
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(box1 != null && box2 != null) { 
					box1.switchBox(box2);	
				}
			}
		};
		return listener;
	}
	
	/**
	 * Listener to handle switching in a CardLayout.
	 * @param cl Target CardLayout
	 * @param parent Target container must contain the CardLayout.
	 * @return Listener that cycles cards.
	 */
	public ActionListener cardSwitch(CardLayout cl, Container parent) {
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cl.next(parent);
			}
		};
		return listener;
	}


	/**
	 * Updates POI list.
	 */
	public void updatePOIlist() {
		frame.updatePOIlist();
	}

	/**
	 * Sets cursor icon
	 * @param cursor The desired cursor.
     */
	public void setCursor(Cursor cursor) {
		frame.setCursor(cursor);
	}

	/**
	 * Returns whether or not the toggle button for car is selected.
	 * @return True for car, false for bike.
     */
	public boolean carSelected() {
		return frame.carSelected();
	}

	/**
	 * Returns whether or not the toggle button for fast is selected.
	 * @return True for fastest route, false for shortest route.
     */
	public boolean fastSelected() {
		return frame.fastSelected();
	}

	/**
	 * Updates the navigation list.
	 */
	public void updateNavList() {
		frame.updateNavList();
	}

	/**
	 * Returns the current config.
	 * @return The current config.
     */
	public Config config() { return mapCtrl.config(); }

	/**
	 * Sets the config.
	 * @param config The desired config.
     */
	public void config(Config config) { mapCtrl.config(config); }
}
