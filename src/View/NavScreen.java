package View;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.OverlayLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import javax.imageio.ImageIO;
import Controller.NavController;
import Controller.Main;
import Model.Model;

/**
 * The overall JFrame of the program.
 */
public class NavScreen extends JFrame implements Observer {
	private static final long serialVersionUID = 16052016L;
	private NavController navCtrl;
	private MapCanvas mapCanvas;
	private Model model; 
	private JPanel overlayPanel, sidePanel, topPanel;
	private JButton printBtn;
	private JToggleButton fastTgl, shortTgl;
	private POIlist poilist;
	private NavigationList navList;
	private AddressFinder fromField, toField;
	private ButtonGroup vehicleGroup, routeTypeGroup; 
	private CardLayout sideCards, topCards; //Stores cards, like nav/search(poi), and settings/navscreen
	private boolean repaintable = false; //Boolean to avoid crash of overlayPanel. Risks call to repaint through model.update(), even if there is no model. (eg. save/load)
	private boolean printEnabled = false, fastEnabled = false;
	private int oldX, oldY; //Old size of frame. Used in navCtrl to rescale when window size changes.

	//For centralized UI-scheme across all windows. Makes it easy to change. 
	public static Color btnBG() { return new Color(100, 100, 100); }	
	public static Color btnFG() { return new Color(255, 255, 255); }
	public static Color panelBG() { return new Color(250, 250, 250); }
	public static Color fromBoxColor() { return new Color(120, 190, 80); }
	public static Color toBoxColor() { return new Color(200, 100, 100); }
	public static Font title() { return new Font("Segoe UI Semibold", Font.PLAIN, 16); }
	public static Font text() { return new Font("Segoe UI Semilight", Font.PLAIN, 12); }

	/**
	 * Instantiates a NavScreen. The main interface of the program.
	 * @param x Width of the JFrame.
	 * @param y Height of the JFrame.
	 * @param control NavController.
	 * @param poiList
	 * @param map MapCanvas.
	 * @param list NavigationList.
     * @param settingsPane
     */
	public NavScreen(int x, int y, NavController control, POIlist poiList, MapCanvas map, NavigationList list, SettingsPane settingsPane) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Kronhjort Inc.");
		setSize(new Dimension(x, y));
		setPreferredSize(new Dimension(x, y));
		setMinimumSize(new Dimension(800, 540));
		
		oldX = x;   oldY = y;
		navCtrl = control;
		this.poilist = poiList;
		mapCanvas = map;
		navList = list;

		model = Main.model();
		model.addObserver(this);

		fromField = new AddressFinder();
		toField = new AddressFinder();

		//Starts multiple private UI build calls
		setContentPane(contentPane(settingsPane));

		addComponentListener(navCtrl.resizeCtrl());
		setVisible(true);
		repaintable = true; //Everything is created, repainting is allowed again
	}

	/**
	 * Creates outer layout. Holds the MapCanvas and the sidePanel.
	 * @return final contentPane of NavScreen
	 */
	private JPanel contentPane(SettingsPane settingsPane) {
		JPanel contentPane = new JPanel();
		
		//Order important! Dependencies on map and controllers add up in this order, another order can result in nullpointer.
		JPanel mapPane = createMap();
		sideCards = new CardLayout(); 
		topCards = new CardLayout();
		JPanel sidePane = sidePanel(settingsPane);
		
		//Combine mapcanvas and overlayButtons in a overlayLayout. 
		overlayPanel = new JPanel();
		overlayPanel.setLayout(new OverlayLayout(overlayPanel));
		overlayPanel.add(overlayButtons());
		overlayPanel.add(mapPane);

		//Automated grouplayout sorcery to avoid automatic resizing of sidepane. 
		GroupLayout group = new GroupLayout(contentPane);
		//Horizontal depencies
		group.setHorizontalGroup( 
			group.createParallelGroup(Alignment.LEADING) 
				.addGroup(Alignment.TRAILING, group.createSequentialGroup()
					.addComponent(overlayPanel, GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE) //Allocate from 500px to MAX_VALUE of space to overlayPanel.
					.addComponent(sidePane, GroupLayout.PREFERRED_SIZE, 340, GroupLayout.PREFERRED_SIZE)) //Lock sidepane to 340px, and let the rest scale to preferredSize. 
		);
		//Vertical depencies
		group.setVerticalGroup(
			group.createParallelGroup(Alignment.LEADING) //Both are allocated from 600px to MAX_VALUE vertical space and will strech in height automatically. 
				.addComponent(sidePane, Alignment.TRAILING, 300, 600, Short.MAX_VALUE)
				.addComponent(overlayPanel, Alignment.TRAILING, 300, 600, Short.MAX_VALUE)
		);
		contentPane.setLayout(group);
		
		return contentPane;
	}
	
	/**
	 * Initializes a JPanel with a MapCanvas.
	 * @return MapPanel.
	 */
	private JPanel createMap() {
		JPanel mapPanel = standardPanel();
		mapPanel.setLayout(new BorderLayout());
		mapPanel.setSize(new Dimension((int)(getWidth()*0.8), getHeight()));
		mapPanel.add(mapCanvas, BorderLayout.CENTER);
		return mapPanel;
	}
	
	/**
	 * Instantiates the main SidePanel. This panel consists of two states:
	 * Either it is a sidePane, or it can switch to show settings.
	 * The sidePane houses search, navigation, the poiList and path-to-text(navigationList).
	 * @return sidePanel.
	 */
	private JPanel sidePanel(SettingsPane settingsPane) {
		//Initialize outer panel and cardLayout
		sidePanel = standardPanel();
		sidePanel.setLayout(sideCards);
		sidePanel.setSize((int)(getWidth()*0.2), this.getHeight());


		//Persistent panel on top, housing the default searchField.
		JPanel fromFieldPanel = new JPanel();
		fromFieldPanel.setLayout(new BorderLayout(0,0));
		fromFieldPanel.setBorder(new EmptyBorder(0,10,0,10));
		fromFieldPanel.setBackground(panelBG());
		fromField.setFont(text());
		JPanel fromColorIndicator = new JPanel();
		fromColorIndicator.setBackground(fromBoxColor());
		fromFieldPanel.add(fromField, BorderLayout.CENTER);
		fromFieldPanel.add(fromColorIndicator, BorderLayout.EAST);
		
		//Calls to create and add the secondary cardLayout of search/navigation and the bottomPanel houseing buttons.
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 0));
		panel.setSize((int)(getWidth()*0.3), getHeight());
		panel.add(fromFieldPanel, BorderLayout.NORTH);
		panel.add(topPanel(), BorderLayout.CENTER);
		panel.add(bottomPanel(), BorderLayout.SOUTH);	
		panel.setBackground(panelBG());
		
		sidePanel.add(panel);
		sidePanel.add(settingsPane);
		return sidePanel;
	}
	
	/**
	 * Creates the topPanel.
	 * CardLayout with 2 cards; one for search and POI and the other for Navigation.
	 * @return topPanel.
	 */
	private JPanel topPanel() {
		topPanel = new JPanel();
		topPanel.setLayout(topCards);
		topPanel.add("Search", searchPanel());
		topPanel.add("Navigate", navigationPanel());
		return topPanel;
	}

	/**
	 * Creates the searchPanel - a cards in the topPanel CardLayout.
	 * Contains two buttons and a poiList.
	 * @return searchPanel.
     */
	private JPanel searchPanel() {
		//Wrapper panel
		JPanel outerPanel = standardPanel();
		outerPanel.setLayout(new BorderLayout(0,5));
		
		//Create buttons and fetch listeners from NavController
		JButton tglNavBtn = customBtn("Toggle Navigation");
		JButton searchBtn = imageBtn("Search", "search");
		tglNavBtn.addActionListener(navCtrl.cardSwitch(topCards, topPanel));
		searchBtn.addActionListener(navCtrl.searchBtnCtrl(fromField));
		
		//Nested panel to hold buttons with margin.
		JPanel searchPanel = standardPanel();
		searchPanel.setBorder(new EmptyBorder(5,5,5,5));
		searchPanel.setLayout(new GridLayout(1,0,5,5));
		searchPanel.add(tglNavBtn);
		searchPanel.add(searchBtn);

		outerPanel.add(searchPanel, BorderLayout.NORTH);
		outerPanel.add(poilist, BorderLayout.CENTER);  //List of POI's is handled in a separate class
		
		return outerPanel;
	}

	/**
	 * Creates the navigationPanel - one of the cards in the topPanel CardLayout.
	 * Contains an additional AddressFinder, two buttons, toggle buttons for fastest/shortest
	 * and car/bike, as well as a directionList.
	 * @return navigationPanel.
     */
	private JPanel navigationPanel() {
		//Wrapper panel
		JPanel outerPanel = standardPanel();
		outerPanel.setLayout(new BorderLayout(0, 5));
		
		//Main panel of the top section. 
		JPanel navigationPanel = new JPanel();
		navigationPanel.setLayout(new BorderLayout(0, 0));
		navigationPanel.setBackground(panelBG());

		//Nested panel for AddressFinder (toField)
		JPanel subTopPanel = standardPanel();
		subTopPanel.setLayout(new BorderLayout(0, 0));

		toField.setFont(text());
		JPanel toColorIndicator = new JPanel();
		toColorIndicator.setBackground(toBoxColor());
		subTopPanel.add(toField, BorderLayout.CENTER);
		subTopPanel.add(toColorIndicator, BorderLayout.EAST);
		
		//Nested panel for buttons and toggles
		JPanel subBottomPanel = new JPanel();
		subBottomPanel.setLayout(new BorderLayout(5, 5));
		subBottomPanel.setBackground(panelBG());
		
		//Deeper nested panel for toggles layout. 
		JPanel tglPanel = standardPanel();
		tglPanel.setLayout(new GridLayout(1, 0, 5, 5));
		
		//Create toggles and set selection behaviour through a buttonGroup
		vehicleGroup = new ButtonGroup();
		routeTypeGroup = new ButtonGroup();
		JToggleButton carTgl = iconTglBtn("car", vehicleGroup);
		JToggleButton cycleTgl = iconTglBtn("cycle", vehicleGroup);
		shortTgl = customTgl("Short", routeTypeGroup);
		fastTgl = customTgl("Fast", routeTypeGroup);

		//Add listeners and actioncommand (Actioncommand is used by btngroup to identify selected button)
		carTgl.setActionCommand("Car");
		carTgl.addActionListener(navCtrl.toggleListener());
		cycleTgl.setActionCommand("Cycle");
		cycleTgl.addActionListener(navCtrl.toggleListener());
		shortTgl.setActionCommand("Short");
		shortTgl.addActionListener(navCtrl.toggleListener());
		fastTgl.setActionCommand("Fast");
		fastTgl.addActionListener(navCtrl.toggleListener());

		tglPanel.add(carTgl);
		tglPanel.add(cycleTgl);
		tglPanel.add(shortTgl);
		tglPanel.add(fastTgl);
		carTgl.setSelected(true);
		fastTgl.setSelected(true);

		//Deeper nested panel for buttons
		JPanel subNavPanel = new JPanel();
		subNavPanel.setLayout(new BorderLayout(5, 0));
		subNavPanel.setBorder(new EmptyBorder(0,5,5,5));
		subNavPanel.setBackground(panelBG());
		
		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(new GridLayout(1, 0, 5, 5));
		btnPanel.setBackground(panelBG());
		
		JButton tglSearchBtn = customBtn("Toggle Search");
		tglSearchBtn.addActionListener(navCtrl.cardSwitch(topCards, topPanel));
		JButton findRoute = imageBtn("Find Route", "search");
		findRoute.addActionListener(navCtrl.findRouteBtnCtrl(fromField, toField));
		JButton switchBtn = switchBtn();
		switchBtn.addActionListener(navCtrl.switchTxtCtrl(fromField, toField));
		
		btnPanel.add(tglSearchBtn);
		btnPanel.add(findRoute);
		subNavPanel.add(switchBtn, BorderLayout.WEST);
		subNavPanel.add(btnPanel, BorderLayout.CENTER);
		
		//Add all panels in nested style, order important!
		subBottomPanel.add(tglPanel, BorderLayout.NORTH);
		subBottomPanel.add(subNavPanel, BorderLayout.CENTER);
		navigationPanel.add(subBottomPanel, BorderLayout.CENTER);
		navigationPanel.add(subTopPanel, BorderLayout.NORTH);
		outerPanel.add(navigationPanel, BorderLayout.NORTH);
		outerPanel.add(navList, BorderLayout.CENTER);
		
		return outerPanel;
	}

	/**
	 * Creates the bottomPanel below the CardLayout.
	 * Contains 6 different buttons.
	 * @return bottomPanel.
     */
	private JPanel bottomPanel() {
		JPanel bottomPanel = standardPanel();
		bottomPanel.setLayout(new GridLayout(3, 0, 5, 5));

		//Create all buttons with icons
		JButton settingsBtn = imageBtn("Settings", "settings");
		JButton addBtn = imageBtn("Add POI", "add");
		JButton helpBtn = imageBtn("Help", "help");
		JButton saveBtn = imageBtn("Save", "save");
		JButton loadBtn = imageBtn("Load", "load");
		printBtn = imageBtn("Print Route", "print");

		printBtn.setEnabled(printEnabled);

		//Fetch listeners from NavController
		settingsBtn.addActionListener(navCtrl.cardSwitch(sideCards, sidePanel));
		addBtn.addActionListener(navCtrl.addCtrl(topCards, topPanel));
		helpBtn.addActionListener(navCtrl.helpCtrl());
		saveBtn.addActionListener(navCtrl.saveCtrl());
		loadBtn.addActionListener(navCtrl.loadCtrl());
		printBtn.addActionListener(navCtrl.printCtrl(fromField, toField));
		
		bottomPanel.add(saveBtn);
		bottomPanel.add(loadBtn);
		bottomPanel.add(helpBtn);
		bottomPanel.add(settingsBtn);
		bottomPanel.add(addBtn);
		bottomPanel.add(printBtn);
		return bottomPanel;
	}

	/**
	 * Creates the overlayButtons - a part of the overlayPanel.
	 * Makes it possible to put a JPanel on top of another JPanel.
	 * Contains a button group of 9 buttons on top of the MapCanvas.
	 * @return overlayButtons.
     */
	private JPanel overlayButtons() {
		JPanel panel = new JPanel();
		//Create buttons with only images.
		JButton zoomOutBtn = imageBtn("zoomout");
		JButton downBtn = imageBtn("down");
		JButton zoomInBtn = imageBtn("zoomin");
		JButton leftBtn = imageBtn("left");
		JButton resetBtn = imageBtn("reset");
		JButton rightBtn = imageBtn("right");
		JButton rotLeftBtn = imageBtn("rotateleft");
		JButton upBtn = imageBtn("up");
		JButton rotRightBtn = imageBtn("rotateright");
		
		//Fetch listeners from NavController
		upBtn.addActionListener(navCtrl.panCtrl("up"));
		downBtn.addActionListener(navCtrl.panCtrl("down"));
		leftBtn.addActionListener(navCtrl.panCtrl("left"));
		rightBtn.addActionListener(navCtrl.panCtrl("right"));
		zoomInBtn.addActionListener(navCtrl.zoomCtrl(true));
		zoomOutBtn.addActionListener(navCtrl.zoomCtrl(false));
		resetBtn.addActionListener(navCtrl.resetCtrl());
		rotLeftBtn.addActionListener(navCtrl.rotateCtrl(false));
		rotRightBtn.addActionListener(navCtrl.rotateCtrl(true));
		
		//GroupLayout sorcery to add buttons in a locked 3x3 grid.
		GroupLayout groupLayout = new GroupLayout(panel);
		groupLayout.setHorizontalGroup( // Creates 3 horizontal rows for buttons
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap(25,25)   
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(zoomOutBtn, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(downBtn, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(zoomInBtn, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(leftBtn, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(resetBtn, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(rightBtn, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(rotLeftBtn, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(upBtn, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(rotRightBtn, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(600, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup( // Creates 3 vertical columns for buttons
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
					.addContainerGap(500, Short.MAX_VALUE)  
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(rotLeftBtn, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
						.addComponent(upBtn, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
						.addComponent(rotRightBtn, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(leftBtn, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
						.addComponent(resetBtn, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
						.addComponent(rightBtn, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(zoomOutBtn, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
						.addComponent(downBtn, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
						.addComponent(zoomInBtn, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(25, 25))
		);
		panel.setLayout(groupLayout);
		panel.setOpaque(false);
		return panel;
	}

	/**
	 * Creates the standardPanel - used as a template JPanel.
	 * @return standardPanel.
     */
	private JPanel standardPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(5,5,5,5));
		panel.setBackground(panelBG());
		return panel;
	}
	
	/**
	 * Creates a square JButton with image on top, no text.
	 * Background and image colour are set according to UI-scheme.
	 * @param filename: The name of the imageFile, without extension (ex. no ".png").
	 * @return Stylized JButton.
     */
	private JButton imageBtn(String filename) {
		JButton btn = new JButton();
		btn.setBackground(btnBG());
		btn.setForeground(btnFG());
		btn.setBorder(new LineBorder(Color.WHITE, 1));
		try {
			InputStream in = getClass().getClassLoader().getResourceAsStream("icon_" + filename + ".png");
			if(in == null) { throw new IOException(); }
			Image inputImg = ImageIO.read(in);
		    Image scaledImg = inputImg.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
		    btn.setIcon(new ImageIcon(scaledImg));
		} catch (IOException ex) {
			//Intentionally does nothing.
		}
		return btn;
	}
	
	/**
	 * Creates a JButton with image and text formatted.
	 * Uses colors from UI-scheme.
	 * @param filename: The name of the imageFile, without extension (ex. no ".png").
	 * @param label: The text that should be on the button.
	 * @return Stylized JButton.
	 */
	private JButton imageBtn(String label, String filename) {
		JButton btn = new JButton(label);
		btn.setForeground(btnFG());
		btn.setBackground(btnBG());
		btn.setFont(text());
		try {
			InputStream in = getClass().getClassLoader().getResourceAsStream("icon_" + filename + ".png");
			if(in == null) { throw new IOException(); }
			Image inputImg = ImageIO.read(in);
		    Image scaledImg = inputImg.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
		    btn.setIcon(new ImageIcon(scaledImg));
		} catch (IOException ex) {
			//Intentionally does nothing.
		}
		return btn;
	}
	
	/**
	 * Used for a special case imageBtn.
	 * @return Stylized JButton.
	 */
	private JButton switchBtn() {
		JButton btn = new JButton();
		btn.setBackground(btnBG());
		btn.setForeground(btnFG());
		btn.setFont(text());
		try {
		    InputStream in = getClass().getClassLoader().getResourceAsStream("icon_switch.png");
			if(in == null) { throw new IOException(); }
			Image inputImg = ImageIO.read(in);
		    Image scaledImg = inputImg.getScaledInstance(15, 30, Image.SCALE_SMOOTH);
		    btn.setIcon(new ImageIcon(scaledImg));
		} catch (IOException ex) {
			//Intentionally does nothing.
		}
		return btn;
	}
	
	/**
	 * Creates a JButton with colors and font according to UI-scheme.
	 * @param label: The text to show on button.
	 * @return Stylized JButton.
	 */
	private JButton customBtn(String label) {
		JButton btn = new JButton(label);
		btn.setFont(text());
		btn.setForeground(btnFG());
		btn.setBackground(btnBG());
		return btn;
	}
	
	/**
	 * Creates a JToggleButton with colors and font according to UI-scheme.
	 * Needs a button-group reference to have the correct selection behaviour.  
	 * @param label: The text to be on the button.
	 * @param btnGroup: The buttonGroup that should be responsible for this toggle. 
	 * @return Stylized Toggle added to the passed Buttongroup. 
	 */
	private JToggleButton customTgl(String label, ButtonGroup btnGroup) {
		JToggleButton tglBtn = new JToggleButton(label);
		tglBtn.setFont(text());
		tglBtn.setForeground(btnFG());
		tglBtn.setBackground(btnBG());
		btnGroup.add(tglBtn);
		return tglBtn;
	}
	
	/**
	 * Creates a JToggleButton with only image on top and colors according to UI-scheme.
	 * Needs a button-group reference to have the correct selection behaviour.  
	 * @param filename: The name of the imageFile, without extension. (ex. no ".png")
	 * @param btnGroup: The buttonGroup that should be responsible for this toggle. 
	 * @return Stylized Toggle added to the passed Buttongroup. 
	 */
	private JToggleButton iconTglBtn(String filename, ButtonGroup btnGroup) {
		JToggleButton tglBtn = new JToggleButton();
		try {		    
		    InputStream in = getClass().getClassLoader().getResourceAsStream("icon_" + filename + ".png");
			if(in == null) { throw new IOException(); }
			Image inputImg = ImageIO.read(in);
		    Image scaledImg = inputImg.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
		    tglBtn.setIcon(new ImageIcon(scaledImg));
		} catch (IOException ex) {
			//Intentionally does nothing.
		}
		
		tglBtn.setBackground(btnBG());
		btnGroup.add(tglBtn);
		return tglBtn;
	}

	/**
	 * Enables print button.
	 * @param b
     */
	public void printBtnEnable(boolean b) {
		printEnabled = b;
		printBtn.setEnabled(printEnabled);
	}

	/**
	 * Returns whether or not the print button is active.
	 * @return
     */
	public boolean printEnabled() {
		return printEnabled;
	}

	/**
	 * Enables the fast toggle button.
	 * @param b
     */
	public void fastTglEnable(boolean b) {
		fastEnabled = b;
		fastTgl.setEnabled(fastEnabled);
	}

	/**
	 * Sets the short toggle button as selected.
	 * @param b
     */
	public void shortSetSelected (boolean b) {
		shortTgl.setSelected(b);
	}

	/**
	 * Returns the old width of the NavScreen.
	 * @return
     */
	public int oldX() {return oldX;}

	/**
	 * Returns the old height of the NavScreen.
	 * @return
     */
	public int oldY() {return oldY;}

	/**
	 * Sets the old width of the NavScreen.
	 * @param x
     */
	public void oldX(int x) {oldX = x;}

	/**
	 * Sets the old height of the NavScreen.
	 * @param y
     */
	public void oldY(int y) {oldY = y;}

	/**
	 * Returns whether or not car is selected.
	 * @return
     */
	public boolean carSelected() { return vehicleGroup.getSelection().getActionCommand().equals("Car"); }

	/**
	 * Returns whether or not fast is selected.
	 * @return
     */
	public boolean fastSelected() { return routeTypeGroup.getSelection().getActionCommand().equals("Fast"); }

	/**
	 * Used to update the NavigationList.
	 */
	public void updateNavList() { navList.updateList(); }

	/**
	 * Used to update the POI list.
	 */
	public void updatePOIlist() { poilist.updatePOI(); }

	/**
	 * Listens for model changes and updates accordingly.
	 * @param arg0
	 * @param arg1
     */
	public void update(Observable arg0, Object arg1) {
		if(repaintable) { overlayPanel.repaint(); }
	}

	/**
	 * Returns the sidePanel CardLayout.
	 * @return
     */
	public CardLayout sideCardLayout() { return sideCards; }

	/**
	 * Returns the sidePanel.
	 * @return
     */
	public JPanel sidePanel() { return sidePanel; }
}
