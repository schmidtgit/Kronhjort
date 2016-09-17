package View;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import Controller.Main;
import Controller.MapController;
import Model.Model;
import Model.UserPOI;
import javax.swing.JList;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;

/**
 *  A JPanel for displaying created UserPOIs in list form.
 */
public class POIlist extends JPanel {
	private Model model; 
	private MapController mapController;
	private JList<UserPOI> list; 
	private JPanel listPanel;
	private MouseListener listener; //Listener saved locally because the list it is on is frequently deleted. 

	/**
	 * Initializes a POIList.
	 * @param mapCtrl MapController to allow zoom to a point in the list
     */
	public POIlist(MapController mapCtrl) {
		model = Main.model();
		mapController = mapCtrl;
		
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout(5, 5));
		setBackground(NavScreen.panelBG());
		
		JLabel topLbl = new JLabel("User-added points of interest:");
		topLbl.setFont(NavScreen.title());
		topLbl.setForeground(NavScreen.btnBG());
		
		//Setup the POI list
		listPanel = new JPanel();
		listPanel.setLayout(new GridLayout(1,1,0,0));
		list = poiList();
		list.addMouseListener(listener = listListener());		
		listPanel.add(new JScrollPane(list));
		add(topLbl, BorderLayout.NORTH);
		add(listPanel, BorderLayout.CENTER);
		setVisible(true);
	}
	
	/**
	 * Updates POI list.
	 */
	public void updatePOI() {
		listPanel.removeAll();
		remove(listPanel);
		list.removeAll();
		list = poiList();
		list.addMouseListener(listener);
		listPanel.add(new JScrollPane(list));
		list.repaint();
		listPanel.updateUI();
		add(listPanel, BorderLayout.CENTER);
		updateUI();
	}
	
	/**
	 * Creates a JList of POI objects with a customized listCellRenderer. 
	 * @return A JList of all POI.
	 */
	private JList<UserPOI> poiList() {
		//Create POI array by copying from model. Has to be a static array for JList to accept it. 
		UserPOI[] userPOIs = new UserPOI[model.userPOI().size()];
		for(int i = 0; i < userPOIs.length; i++) {userPOIs[i] = (UserPOI) model.userPOI().get(i);}
		JList<UserPOI> poiList = new JList<UserPOI>(userPOIs);
		poiList.setCellRenderer(new ImageListRenderer()); //Use custom look
		poiList.addMouseListener(listener);
		return poiList;
	}

	/**
	 * Listener used to zoom to POI upon double click.
	 * @return listener to zoom.
     */
	private MouseListener listListener() {
		MouseAdapter listener = new MouseAdapter() {
		    public void mouseClicked(MouseEvent evt) {
		    	//Filter for only double-clicks and make sure the list is not empty (avoid crash!)
		        if(evt.getClickCount() == 2 && list.getComponentCount() > 0) {
		            mapController.zoomToPoint(list.getSelectedValue(), 100);
		        }
		    }
		};
		return listener;
	}

	public class ImageListRenderer extends DefaultListCellRenderer {
	    @Override
	    public Component getListCellRendererComponent( //This is called whenever JList has to draw its cells. 
	            JList list, Object value, int index,
	            boolean isSelected, boolean cellHasFocus) {

	    	//Label component from default reused, but with added image an custom font. 
	        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	        label.setIcon(new ImageIcon(IconPack.userPOIIcon(((UserPOI) value).type())));  //Retrieve matching icon from iconPack.
	        label.setHorizontalTextPosition(JLabel.RIGHT);
	        label.setFont(NavScreen.text());
	        return label;
	    }
	}
}
