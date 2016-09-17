package View;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import Controller.Main;
import Controller.MapController;
import enums.POIType;
import Model.UserPOI;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JTextArea;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * Displays a small JFrame popup with all the required fields to create a userPOI.
 */
public class POIpopup extends JFrame {
	private JTextField titleField;  //POI title
	private JTextArea descField;    //POI description
	private MapController mapCtrl;  //Reference back to MapController that spawned this. 
	private Point2D.Float location; //Geographical location in model coords. 
	private JToggleButton[] toggleBtns; //Icon-selector buttons
	private ButtonGroup btnGroup;  //Manages selection behavior of icon-selector buttons.
	private UserPOI currentPOI;  //Optional parameter, only used when not creating new point. 
	private JPanel btnPanel;   
	private ActionListener btnListener = btnListener();

	/**
	 * Shows the popup based on an existing point. 
	 * @param x: Horizontal screen coordinate for where it should spawn.
	 * @param y: Vertically screen coordinate for where it should spawn.
	 * @param control: MapController reference, so it can update and save point.
	 * @param poi: The POI this popup should reflect. (The one the user clicked)
	 */
	public POIpopup(int x, int y, MapController control, UserPOI poi) {
		this(x, y, control, (float)poi.getX(), (float)poi.getY());
		setSize(260, 400);
		//Fill in information from existing point
		currentPOI = poi;
		titleField.setText(poi.name());
		descField.setText(poi.description());
		for(JToggleButton tgl : toggleBtns) { //Run through icons and find match
			tgl.setSelected(false);
			if(tgl.getActionCommand().equals(poi.type().toString())) { 
				tgl.setSelected(true);
			};
		}
		enableDelete(); //Adds the delete button to layout. 
		reframe();
	}
	
	/**
	 * Shows popup when a new point is to be created.
	 * @param x: Horizontal screen coordinate for where it should spawn.
	 * @param y: Vertically screen coordinate for where it should spawn.
	 * @param control: MapController reference, so it can update and save point.
	 * @param locX: in model x-coordinate.
	 * @param locY: in model y-coordinate.
	 */
	public POIpopup(int x, int y, MapController control, float locX, float locY) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); //Don't close entire program!
		setTitle("Add point of interest"); 
		setBounds(x, y, 220, 360);
		setUndecorated(true); //Remove title bar and borders. 
		setResizable(false);
		setAlwaysOnTop(true);
		
		mapCtrl = control;
		location = new Point2D.Float(locX, locY);
		
		//Overall layout container, with a top, middle and bottom section. 
		JPanel contentPane = new JPanel(); 
		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		contentPane.setLayout(new BorderLayout(10, 10));
		contentPane.setBackground(NavScreen.panelBG());
		setContentPane(contentPane);  //Set in place of default contentpane. 
		
		contentPane.add(topPanel(), BorderLayout.NORTH);
		contentPane.add(centerPanel(), BorderLayout.CENTER);
		contentPane.add(bottomPanel(), BorderLayout.SOUTH);
		reframe();
		setVisible(true);
		titleField.requestFocus(); //Try to make cursor ready at first input field.
		
	}
	
	/**
	 * Top-section of layout. 
	 * Houses POI title, titlefield and a closestroad label. 
	 * @return The populated topPanel design.
	 */
	private JPanel topPanel() {
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout(0, 5));
		topPanel.setBackground(NavScreen.panelBG());
		
		//Create title input field. 
		titleField = new JTextField();
		titleField.setText("Type in name");
		titleField.setHorizontalAlignment(SwingConstants.CENTER);
		titleField.setColumns(10);
		titleField.setFont(NavScreen.title());
		titleField.addMouseListener(txtListener()); //Removes default text on click.
		
		//Find and insert closest road label, if any. 
		JLabel locationLbl = new JLabel();
		try {
			String roadName = Main.model().closestRoad(location).name();
			locationLbl.setText("Near " + roadName);
		} catch(NullPointerException e) { locationLbl.setText("(No closest road found)"); }
		
		locationLbl.setHorizontalAlignment(SwingConstants.CENTER);
		locationLbl.setFont(NavScreen.text());
		locationLbl.setForeground(NavScreen.btnBG());
		
		topPanel.add(titleField, BorderLayout.SOUTH);
		topPanel.add(locationLbl, BorderLayout.NORTH);
		return topPanel;
	}
	
	/**
	 * Center section layout.
	 * Houses the multiline POI description field. 
	 * @return the populated centerPanel design.
	 */
	private JPanel centerPanel() {
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout());
		centerPanel.setBorder(new LineBorder(NavScreen.btnBG(), 1));
		
		descField = new JTextArea();
		descField.setText("Type in description");
		descField.setFont(NavScreen.text());
		descField.setLineWrap(true);
		descField.addMouseListener(txtListener());
		
		centerPanel.add(descField);
		return centerPanel;
	}
	
	/**
	 * Bottom section of layout.
	 * Houses a subdivide, with a panel of buttons and a grid panel of icon toggles.
	 * @return the populated bottomPanel design. 
	 */
	private JPanel bottomPanel() {
		//Wrapper panel
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		
		//Panel for icon-grid
		JPanel iconPanel = new JPanel();
		iconPanel.setLayout(new GridLayout(3, 0, 5, 5));
		iconPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
		iconPanel.setBackground(NavScreen.panelBG());
		
		//Instantiate a toggle for each POI-type and add to a selection model.
		btnGroup = new ButtonGroup();
		toggleBtns = new JToggleButton[14];
		int i = 0;
		for(POIType type: POIType.values()) {
			toggleBtns[i] = new JToggleButton(new ImageIcon(IconPack.userPOIIcon(type)));
			toggleBtns[i].setBackground(NavScreen.btnFG());
			toggleBtns[i].setActionCommand(type.toString());
			btnGroup.add(toggleBtns[i]);
			iconPanel.add(toggleBtns[i]);
			i++;
		}
		toggleBtns[0].setSelected(true);
		
		//Panel for ok, cancel and delete buttons.
		btnPanel = new JPanel();
		btnPanel.setLayout(new GridLayout(1, 2, 5, 0));
		btnPanel.setBackground(NavScreen.panelBG());
		btnPanel.add(customBtn("OK"));
		btnPanel.add(customBtn("Cancel"));
		
		//Add inner panels to wrapper panel
		bottomPanel.add(iconPanel, BorderLayout.NORTH);
		bottomPanel.add(btnPanel, BorderLayout.SOUTH);
		return bottomPanel;
	}
	
	/**
	 * Enables a delete button. (Requires a currentPOI)
	 */
	private void enableDelete() {
		btnPanel.add(customBtn("Delete"));	
		btnPanel.updateUI();
		btnPanel.repaint();
	}
	
	/**
	 * Creates a styled JButton with an ActionListener.
	 * @param label The text on the button.
	 * @return A stylized JButton with the given text. 
	 */
	private JButton customBtn(String label) {
		JButton btn = new JButton(label);
		btn.setBackground(NavScreen.btnBG());
		btn.setForeground(NavScreen.btnFG());
		btn.setFont(NavScreen.text());
		btn.addActionListener(btnListener);
		return btn;
	}
	
	/**
	 * Look-up of POIType based on a search String.
	 * Used by icon toggle buttons in order to determine what kind of icon type,
	 * their selection should result in.
	 * @param actionCommand Text from iconBtn
	 * @return The matched POIType, or "POIType.UNKNOWN" if search misses. 
	 */
	private POIType matchPOIType(String actionCommand) {
		for(POIType type : POIType.values()) {
			if(type.toString().equals(actionCommand)) {return type;}
		}
		return POIType.UNKNOWN;
	}
	
	private void reframe() {
		//Popupframe parameters
		Rectangle frameSize = getBounds();
		int endWidth = (int) frameSize.getMaxX();
		int endHeight = (int) frameSize.getMaxY();
		
		//Screen parameters
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int) screenSize.getWidth();
		int height = (int) screenSize.getHeight();
		
		//Move back inside horizontal axis.
		if(endWidth > width) {
			int delta = endWidth-width;
			setLocation(new Point((int)(frameSize.getX()-delta), (int)frameSize.getY()));
		}
		//Move back inside vertical axis. 
		if(endHeight > height) {
			int delta = endHeight-height;
			setLocation(new Point((int)frameSize.getX(), (int) (frameSize.getY()-delta)));
		}
	}
	
	
	/**
	 * Listener for bottom row of buttons - ok, cancel and delete.
	 * @return Listener for ok, cancel and delete.
	 */
	private ActionListener btnListener(){
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e)
            {
				if(e.getSource() instanceof JButton) {
					JButton btn = (JButton) e.getSource();
					if(btn.getText().equals("OK")) {
						//Delete current point and recreate a new version based on input. 
						Main.model().deleteUserPOI(currentPOI);
						String name = titleField.getText();
			            String desc = descField.getText();
			            POIType type = matchPOIType(btnGroup.getSelection().getActionCommand());
			            UserPOI poi = new UserPOI(location, name, desc, type);
			            mapCtrl.addPOI(poi);	
			            mapCtrl.notifyPopupClosed();
		            	dispose();
					} else if (btn.getText().equals("Delete")) {
						//Remove currentPOI
						Main.model().deleteUserPOI(currentPOI);
						mapCtrl.addPOI(null); //Functions as updating POIlist if the point was deleted
						mapCtrl.notifyPopupClosed();
						dispose();
					} else { 
						//Just close without doing anything
						mapCtrl.notifyPopupClosed();
						dispose(); 
					}
				}					
				Main.model().update();
            }
		};
		return listener;
	}
	
	/**
	 * Listener to remove the default text in the input fields upon first click.
	 * @return Listener to clear default text.
	 */
	private MouseListener txtListener(){
		MouseAdapter listener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e)
            {
				Object source = e.getSource();
				if(source instanceof JTextField) {
					if(titleField.getText().equals("Type in name")) {titleField.setText("");}
				} else if (source instanceof JTextArea) {
					if(descField.getText().equals("Type in description")) {descField.setText("");}
				}
            }
		};
		return listener;
	}

}
