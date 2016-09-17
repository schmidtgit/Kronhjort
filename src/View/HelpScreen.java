package View;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Font;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * JFrame with tabbed help panels that describes how to use program.
 */
public class HelpScreen extends JFrame {	
	public HelpScreen() {
		setTitle("Help");
		setSize(800, 540);
		setPreferredSize(new Dimension(800, 640));
		setMinimumSize(new Dimension(480, 360));
		setAlwaysOnTop(true);
			
		//Main layout is a tabbedPane, where all helpsections are added as JPanels under this.
		JTabbedPane tabPane = new JTabbedPane(JTabbedPane.TOP);
		tabPane.setTabPlacement(JTabbedPane.LEFT);
		tabPane.setFont(NavScreen.text());
		tabPane.setBackground(NavScreen.btnBG());
		tabPane.setForeground(NavScreen.btnFG());
		getContentPane().add(tabPane, BorderLayout.CENTER);
			
		//Add the tabs through methods
		tabPane.addTab("Key Bindings", null, keyTab(), null);
		tabPane.addTab("Using the map", null, mapTab(), null);
		tabPane.addTab("Searching", null, searchTab(), null);
		tabPane.addTab("Navigation", null, navTab(), null);
		tabPane.addTab("User POI", null, poiTab(), null);
		tabPane.addTab("Using settings", null, settingsTab(), null);
		
		//Make it spawn in center of screen
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		setVisible(true);
	}
		
	/**
	 * Create a JPanel to fill out the key-shortcut description tab.
	 * @return populated and stylized keyTab (JPanel) ;
	 */
	private JPanel keyTab() {
		//All keys and descriptions are stored in to parallel string-arrays.
		//Makes it easy to add a lot of labels quickly.
		String[] keyList = {"w","s","a","d","q","e","g","h","t","y","u","i","o","p"};
		String[] descriptions = {"Move map up", "Move map down", "Move map left", "Move map right",
				"Rotate map left", "Rotate map right", "Zoom to center of map", "Show the helpscreen", "[Debug tool] Toggle test viewport",
				"[Debug tool] Show/Hide fps-counter", "[Debug tool] Toggle alpha-fading", "[Debug tool] Toggle compression-view on buildings", 
				"[Debug tool] Toggle anti-aliasing", "[Debug tool] Toggle OpenGL"};
		Font keyFont = new Font("Segoe UI Semibold", Font.BOLD, 18);
		Font descFont = new Font("Segoe UI Semilight", Font.PLAIN, 16);
			
		//Outer panel
		JPanel keyTab = new JPanel();
		keyTab.setLayout(new BorderLayout());
			
		//Left panel to hold all key-labels.
		JPanel keyPanel = new JPanel();
		keyPanel.setLayout(new GridLayout(keyList.length, 1));
		keyPanel.setBackground(NavScreen.panelBG());
		keyPanel.setBorder(new EmptyBorder(10, 40, 10, 60));

		//Right panel to hold all description-labels.
		JPanel descPanel = new JPanel();
		descPanel.setLayout(new GridLayout(keyList.length, 1));
		descPanel.setBackground(NavScreen.panelBG());
		descPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		//Fill in all key & description labels based on keyList & descriptions.
		for(int i = 0; i < keyList.length; i++) {
			JLabel keylabel = new JLabel(keyList[i].toUpperCase());
			keylabel.setFont(keyFont);
			keylabel.setForeground(NavScreen.btnBG());

			JLabel desclabel = new JLabel(descriptions[i]);
			desclabel.setFont(descFont);

			keyPanel.add(keylabel);
			descPanel.add(desclabel);
		}
		keyTab.add(keyPanel, BorderLayout.WEST);
		keyTab.add(descPanel, BorderLayout.CENTER);
			
		return keyTab;
	}
		
	/**
	 * Create a JPanel to fill out the "how to use the map" tab.
	 * @return populated and stylized mapTab (JPanel) ;
	 */
	private JPanel mapTab() {
		//Outer panel
		JPanel mapTab = new JPanel();
		mapTab.setLayout(new BorderLayout(5,5));
		mapTab.setBorder(new EmptyBorder(5,5,5,5));
		mapTab.setBackground(NavScreen.panelBG());
		
		//Left panel to hold images
		JPanel imageColumn = new JPanel();
		imageColumn.setLayout(new GridLayout(0,1,5,5));
		imageColumn.setBorder(new EmptyBorder(10,40,10,60));
		imageColumn.setBackground(NavScreen.panelBG());
		imageColumn.add(imageLbl("navwheel_help"));
		imageColumn.add(imageLbl("pan_help"));
		imageColumn.add(imageLbl("zoom_help"));
		
		//Right panel to hold descriptive text
		JPanel txtColumn = new JPanel();
		txtColumn.setLayout(new GridLayout(0,1,5,5));
		txtColumn.setBackground(NavScreen.panelBG());
		txtColumn.add(styledLbl("Use the navigationbuttons to move around the map"));
		txtColumn.add(styledLbl("Hold and drag with mouse to pan"));
		txtColumn.add(styledLbl("Use mousewheel or 2 finger scrolling to zoom"));
		
		mapTab.add(imageColumn, BorderLayout.WEST);
		mapTab.add(txtColumn, BorderLayout.CENTER);
		return mapTab;
	}
		
	/**
	 * Create a JPanel to fill out the search section. Uses only pictures. 
	 * @return populated and stylized searchTab (JPanel) ;
	 */
	private JPanel searchTab() {
		//Outer panel
		JPanel searchTab = new JPanel();
		searchTab.setBackground(NavScreen.panelBG());
		searchTab.setLayout(new GridLayout(0, 1, 20, 50));
		searchTab.setBorder(new EmptyBorder(25, 25, 25, 25));
		
		//Layout-panel to force a split in the upper section of this tab. 
		JPanel topPanel = new JPanel();
		topPanel.setBackground(NavScreen.panelBG());
		topPanel.setLayout(new GridLayout(1, 0, 20, 20));
		
		//Create images, 2 are added to the top split, and 1 on the bottom that streches accross.
		ImagePanel leftPanel = new ImagePanel("search_help1");
		ImagePanel rightPanel = new ImagePanel("search_help2");
		ImagePanel bottomPanel = new ImagePanel("search_help3");
		bottomPanel.setBorder(new EmptyBorder(25, 10, 10, 10));
		topPanel.add(leftPanel);
		topPanel.add(rightPanel);
		searchTab.add(topPanel);
		searchTab.add(bottomPanel);
		
		return searchTab;
	}
		
	/**
	 * Create a JPanel to fill out the navigation section.
	 * @return populated and stylized navTab (JPanel) ;
	 */
	private JPanel navTab() {
		JPanel navTab = new JPanel();
		navTab.setBackground(NavScreen.panelBG());
		navTab.setLayout(new GridLayout(2, 2, 20, 20));
		navTab.setBorder(new EmptyBorder(10, 10, 10, 10));
		navTab.add(new ImagePanel("nav_help1"));
		navTab.add(new ImagePanel("nav_help2"));
		navTab.add(new ImagePanel("nav_help3"));
		navTab.add(new ImagePanel("nav_help4"));
		return navTab;
	}
	
	/**
	 * Create JPanel that should fill the POI section
	 * @return ready-to-go POI help panel.
	 */
	private JPanel poiTab() {
		JPanel poiTab = new JPanel();
		poiTab.setBackground(NavScreen.panelBG());
		poiTab.setLayout(new BorderLayout(10,10));
		poiTab.setBorder(new EmptyBorder(10,10,10,10));
		
		JPanel topPanel = new JPanel();
		topPanel.setBackground(NavScreen.panelBG());
		topPanel.setLayout(new BorderLayout(10,10));
		
		JPanel iconRow = new JPanel();
		iconRow.setBackground(NavScreen.panelBG());
		iconRow.setLayout(new GridLayout(1, 2, 0, 0));
		iconRow.add(imageLbl("UserPOI_star"));
		iconRow.add(imageLbl("POI_star"));
		
		JPanel labelRow = new JPanel();
		labelRow.setBackground(NavScreen.panelBG());
		labelRow.setLayout(new GridLayout(3, 2, 0, 0));
		JLabel stdPOI = styledLbl("Points of interest look like this");
		stdPOI.setFont(new Font("Segoe UI Semibold", Font.BOLD, 16));
		JLabel userPOI = styledLbl("User made POI look like this");
		userPOI.setFont(new Font("Segoe UI Semibold", Font.BOLD, 16));
		labelRow.add(stdPOI);
		labelRow.add(userPOI);
		labelRow.add(styledLbl("Standard POI cannot be clicked or altered"));
		labelRow.add(styledLbl("UserPOI are usermade, moveable and deletable"));
		labelRow.add(styledLbl("These are only visible when zoomed in close")); 
		labelRow.add(styledLbl("Click and hold to drag, click to alter or delete"));
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setBackground(NavScreen.panelBG());
		bottomPanel.setBorder(new EmptyBorder(20, 10, 10, 0));
		bottomPanel.setLayout(new GridLayout());
		bottomPanel.add(new ImagePanel("poi_help"));
		
		topPanel.add(iconRow, BorderLayout.CENTER);
		topPanel.add(labelRow, BorderLayout.NORTH);
		poiTab.add(topPanel, BorderLayout.NORTH);
		poiTab.add(bottomPanel, BorderLayout.CENTER);
		
		return poiTab;
	}
		
	/**
	 * Create a JPanel to fill out how to use the settings part of the program.
	 * @return populated and stylized settingsTab (JPanel) ;
	 */
	private JPanel settingsTab() {
		JPanel settingsTab = new JPanel();
		settingsTab.setBackground(NavScreen.panelBG());
		settingsTab.setLayout(new GridLayout(1, 0, 20, 20));
		settingsTab.setBorder(new EmptyBorder(10, 25, 10, 15));
		settingsTab.add(new ImagePanel("settings_help1"));
		settingsTab.add(new ImagePanel("settings_help2"));
		return settingsTab;
	}
	
	/**
	 * Creates a stylises JLabel, with colours mathing those of navscreen and a sans serif font. 
	 * @param txt: What text to write on label
	 * @return stylized JLabel
	 */
	private JLabel styledLbl(String txt) {
		JLabel lbl = new JLabel(txt);
		lbl.setFont(new Font("Segoe UI Semilight", Font.PLAIN, 16));
		lbl.setForeground(NavScreen.btnBG());
		return lbl;
	}
	
	/**
	 * Uses a JLabel to create an 128x128 px image component.
	 * Designed to be used with a gridLayout with at least 128px available per cell. 
	 * Is not guaranteed to scale properly in other situations.
	 * @param filename: the name of the file on disk. Do not include file ending (eg. no ".png").
	 * @return JLabel with only an image on it.
	 */
	private JLabel imageLbl(String filename) {
		JLabel lbl = new JLabel();
		lbl.setHorizontalAlignment(JLabel.CENTER);
		try {
			InputStream in = getClass().getClassLoader().getResourceAsStream(filename + ".png");
			if(in == null) { throw new IOException(); }
			Image inputImg = ImageIO.read(in);
		    Image scaledImg = inputImg.getScaledInstance(128, 128, Image.SCALE_SMOOTH);
		    lbl.setIcon(new ImageIcon(scaledImg));
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(null, "Problem while loading an image for the help screen.",
					"Load failed", JOptionPane.ERROR_MESSAGE);
		}
		return lbl;
	}
	
	class ImagePanel extends JPanel {
		private Image img;

		/**
		 * Creates a JPanel with a custom picture as background.
		 * @param filename: the filename of image on disk. Has to be a ".png", but don't
		 *                  write this. Only the name. (eg. "search_help1" not "search_help1.png")
		 */
		public ImagePanel(String filename) {
			try {
				//Get imagelink and load
				InputStream in = getClass().getClassLoader().getResourceAsStream(filename + ".png");
				if(in == null) { throw new IOException(); }
				img = ImageIO.read(in);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Problem while loading an image for the help screen.",
						"Load failed", JOptionPane.ERROR_MESSAGE);
			}
			setBorder(new EmptyBorder(10,10,10,10));
			setBackground(NavScreen.panelBG());
		}

		public void paintComponent(Graphics gg) {
			Graphics2D g = (Graphics2D) gg;
			Image scaledImg; //The final image that will be shown
			
			//Get image width/height properties
			int iW = img.getWidth(null);
			int iH = img.getHeight(null);
			
			//Get image-ratios of image and panel
			double iRatio = (iW + 0.0) / (iH + 0.0); 
			double panelRatio = (getWidth() + 0.0) / (getHeight() + 0.0);
			
			if(iRatio > panelRatio) {
				//Image wider than panel, scale to panel width. 
				double factor = (getWidth() + 0.0)/(iW + 0.0);
				int height = (int) (factor * iH); 
				scaledImg = img.getScaledInstance(getWidth(), height, Image.SCALE_SMOOTH);
			} else {
				//Image heigher than panel, scale to panel height.
				double factor = (getHeight() + 0.0)/(iH + 0.0);
				int width = (int) (factor * iW); 
				scaledImg = img.getScaledInstance(width, getHeight(), Image.SCALE_SMOOTH);
			}	
			g.drawImage(scaledImg, 0, 0, null);
		}
	}
}

