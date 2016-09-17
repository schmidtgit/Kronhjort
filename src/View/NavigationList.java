package View;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.JList;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import Controller.Main;
import Controller.MapController;
import Controller.NavController;
import Model.Road;

public class NavigationList extends JPanel {
	private JList<Direction> list;
	private JPanel listPanel;
	private MouseListener listener;
	private MapController mapCtrl;
	private NavController navCtrl;
	private Road[] path;
	private ArrayList<Direction> dirs;
	private JLabel estimatedTime, estimatedLength;

	/**
	 * Creates a JList in the sidePanel of the GUI.
	 */
	public NavigationList(MapController mapcontrol, NavController control) {
		mapCtrl = mapcontrol;
		listener = listListener();
		navCtrl = control;
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout(5, 5));
		setBackground(NavScreen.panelBG());

		JLabel topLbl = new JLabel("Navigations are shown below:");
		topLbl.setFont(NavScreen.title());
		topLbl.setForeground(NavScreen.btnBG());

		JPanel bottomPanel = new JPanel();
		bottomPanel.setBackground(NavScreen.panelBG());
		bottomPanel.setLayout(new GridLayout(1,0,5,0));
		bottomPanel.setBorder(new EmptyBorder(5,0,5,0));

		estimatedLength = new JLabel();
		estimatedLength.setFont(NavScreen.text());
		estimatedLength.setForeground(NavScreen.btnBG());
		estimatedTime = new JLabel();
		estimatedTime.setFont(NavScreen.text());
		estimatedTime.setForeground(NavScreen.btnBG());

		bottomPanel.add(estimatedLength);
		bottomPanel.add(estimatedTime);

		listPanel = new JPanel();
		listPanel.setLayout(new GridLayout(1,1,0,0));
		list = directionList();
		listPanel.add(new JScrollPane(list));
		add(topLbl, BorderLayout.NORTH);
		add(listPanel, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
		setVisible(true);
	}

	/**
	 * Returns the Directions.
	 * @return
     */
	public ArrayList<Direction> getDirections() { return dirs; }

	/**
	 * Creates an ArrayList of the Direction objects for the given path.
	 * @return If the path is null, the method will return an empty ArrayList,
	 * 		   which will just create an empty NavigationList in the GUI.
	 */
	public ArrayList<Direction> createDirections() {
		ArrayList<Direction> directions = new ArrayList<>();
		if (path == null) { return directions; }
		Road currentRoad = path[path.length - 1];
		int tempLength = currentRoad.length();
		int roadsBefore = 0;
		boolean isRoundabout = false;
		for (int i = path.length - 1; i > 0; i--) {
			Road nextRoad = path[i - 1];
			if(isRoundabout == true && !nextRoad.isRoundabout()) { roadsBefore++; }
			//If the two Roads do not share a vertex, it will just skip that Direction.
			if(currentRoad.sharedVertex(nextRoad).equals(null)) { continue; }
			//This if statement catches all Roundabout Roads.
			if(nextRoad.isRoundabout()) {
				isRoundabout = true;
				for (Road roads : currentRoad.sharedVertex(nextRoad).edges(true)) {
					if (!roads.isRoundabout() && roads != currentRoad) { roadsBefore++; }
				}
				currentRoad = nextRoad;
			} else if(roadsBefore != 0) {
				Point2D intersection = path[i].toList().get(0);
				directions.add(new Direction(intersection, path[i - 1], roadsBefore, tempLength));
				currentRoad = nextRoad;
				tempLength = currentRoad.length();
				roadsBefore = 0;
				isRoundabout = false;
			} else {
				if(!currentRoad.name().equals(nextRoad.name())) {
					directions.add(new Direction(path[i], path[i - 1], tempLength));
					currentRoad = nextRoad;
					tempLength = currentRoad.length();
					roadsBefore = 0;
				} else {
					tempLength += nextRoad.length();
					currentRoad = nextRoad;
				}
			}

		}
		//This post for-loop code creates the final destination Direction.
		Point2D destination = Main.model().pathTo();
		directions.add(new Direction(destination, tempLength));
		return directions;
	}

	/**
	 * Converts the ArrayList into a Direction[] for the JList.
	 * @return
	 */
	private JList<Direction> directionList() {
		dirs = createDirections();
		Direction[] directions = new Direction[dirs.size()];
		dirs.toArray(directions);
		JList<Direction> directionsList = new JList<>(directions);
		directionsList.setCellRenderer(new ImageListRenderer());
		directionsList.addMouseListener(listener);
		return directionsList;
	}

	/**
	 * Estimates the total length of the path.
	 * @return
	 */
	public int estimateLength() {
		int total = 0;
		for(Road road : path) { total += road.length(); }
		return total;
	}

	/**
	 * Estimates the total travel time of the path.
	 * It takes the transportation choice into account.
	 * @return
	 */
	public String estimateTime() {
		float total = 0;
		int hours = 0; 
		if(navCtrl.carSelected()) { //Use precalculated traveltime
			for(Road road : path) { total = total + road.travelTime(); }
		} else { //If bike, just consider the general speed of 15 km/h
			for(Road road : path) { total = total + road.length(); }
			float mPerMin = 15f * 16.66f;
			total = total/mPerMin;
		}
		int result = (int) total;
		while(result > 60) { 
			hours++; 
			result = result - 60;
		}
		return hours + "t " + result + "min";
		
	}

	/**
	 * Update the NavigationList.
	 */
	public void updateList() {
		if(Main.model().path() != null) {
			if(Main.model().path().length != 0) {
				//If a path is present, clear panel + list, and fill in new path. 
				if (Main.model().path()[0].name().contains("[WALKWAY]")) { path = Arrays.copyOfRange(Main.model().path(), 1, Main.model().path().length - 1); } 
				else { path = Main.model().path(); }
				list.removeMouseListener(listener);
				listPanel.removeAll();
				list.removeAll();
				list = directionList();
				listPanel.add(new JScrollPane(list));
				listPanel.updateUI();
				listPanel.repaint();
				
				//Rounds to kilometers and nearest 50m.
				int estLength = estimateLength();
				int kilometers = estLength / 1000; 
				int meters = estLength % 1000;
				meters = (meters / 50) * 50;
				
				//Updates estimation labels in bottom of panel:
				estimatedLength.setText("Est. length: " + kilometers + "." + meters + " km");
				estimatedTime.setText("Est. time: " + estimateTime());
			} 	
		} else {
			listPanel.removeAll();
			list.removeAll();
			listPanel.add(new JScrollPane(null));
			listPanel.updateUI();
			listPanel.repaint();
		}
	}

	/**
	 * Listener to find zoom to Direction intersection.
	 * @return
	 */
	private MouseListener listListener() {
		MouseAdapter  listener = new MouseAdapter() {
		    public void mouseClicked(MouseEvent evt) {
		        if (evt.getClickCount() == 2 && list.getComponentCount() > 0) {
		        	Direction dir = list.getSelectedValue();
		            mapCtrl.zoomToPoint(dir.intersection(), 400);
		        }
		    }
		};
		return listener;
	}

	public class ImageListRenderer extends DefaultListCellRenderer {
	    @Override
	    public Component getListCellRendererComponent(
	            JList list, Object value, int index,
	            boolean isSelected, boolean cellHasFocus) {
	        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	        label.setIcon(new ImageIcon(IconPack.getArrow(((Direction) value).arrow())));
	        label.setHorizontalTextPosition(JLabel.RIGHT);
	        label.setFont(NavScreen.text());
	        return label;
	    }
	}
}