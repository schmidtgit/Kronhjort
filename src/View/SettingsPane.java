package View;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import Controller.Main;
import Controller.NavController;
import Controller.SettingsController;
import Model.Config;
import Model.Model;
import enums.ConfigType;
import enums.ControlType;

/**
 * A JPanel for settings on map with pre-made configs..
 * Changes colours and visibility of map elements.
 */
public class SettingsPane extends JPanel {
	private static final long serialVersionUID = 16052016L;
	SettingsController settingsCtrl;
	NavController navController;
	Model model;
	JComboBox<Config> configBox; //Combobox to hold the configs that can be selected
	JTabbedPane tabPane; //Container for all color and visibility components.
	JButton backBtn;

	/**
	 * Initialises the SettingsPane for insertion in a CardLayout.
	 * @param control SettingsController
	 * @param navControl NavController
     */
	public SettingsPane(SettingsController control, NavController navControl) {
		setLayout(new BorderLayout(0, 0));
		setBorder(new EmptyBorder(0,5,5,5));
		model = Main.model();
		settingsCtrl = control;
		navController = navControl;
		
		//Use config from map. 
		setBackground(NavScreen.panelBG());
		settingsCtrl.config(navControl.config());
		
		//Setup tabs
		tabPane = new JTabbedPane(JTabbedPane.TOP);
		tabPane.setFont(NavScreen.text());
		tabPane.setBackground(NavScreen.btnBG());
		tabPane.setForeground(NavScreen.btnFG());
		tabPane.add("Roads", createTab("Roads"));
		tabPane.add("Nature", createTab("Nature"));
		tabPane.add("Urban", createTab("Urban"));
		
		add(tabPane, BorderLayout.CENTER);
		add(bottomPanel(), BorderLayout.SOUTH);
		
		updateList();
	}

	/**
	 * Creates JComboBox - cycles through configs in model.
	 * @return Completed ConfigSelector
	 */
	private JComboBox<Config> configSelector(){
		configBox = new JComboBox<Config>();
		configBox.setFont(NavScreen.text());
		for(Config con : model.config()) {  //Load configs from models list. 
			configBox.addItem(con);
		}
		configBox.setSelectedItem(navController.config());  //Select the one currently in use by map. (for visual coherence)
		return configBox;
	}
	
	/**
	 * Creates panel for bottom part of the settingsPane.
	 * Contains buttons for back, delete and new, and ConfigSelector box.
	 * @return a ready to go BottomPanel. 
	 */
	private JPanel bottomPanel() {
		//Setup outer panel
		JPanel bottomPanel = new JPanel();
		bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
		bottomPanel.setLayout(new BorderLayout(0, 5));
		bottomPanel.setBackground(NavScreen.panelBG());
		bottomPanel.add(configSelector(), BorderLayout.CENTER);

		JLabel configLbl = new JLabel("Choose mapstyle");
		configLbl.setFont(NavScreen.title());
		configLbl.setForeground(NavScreen.btnBG());
		bottomPanel.add(configLbl, BorderLayout.NORTH);
		
		//Nested panel for arrangement of buttons
		JPanel subBottomPanel = new JPanel();
		subBottomPanel.setLayout(new GridLayout(1,0,10,5));
		subBottomPanel.setBorder(new EmptyBorder(5,0,0,0));
		subBottomPanel.setBackground(NavScreen.panelBG());
		bottomPanel.add(subBottomPanel, BorderLayout.SOUTH);
		
		//Create and style buttons:
		backBtn = new JButton("Back");
		backBtn.setFont(NavScreen.text());
		backBtn.setForeground(NavScreen.btnFG());
		backBtn.setBackground(NavScreen.btnBG());
		
		JButton loadBtn = new JButton("Load");
		loadBtn.setFont(NavScreen.text());
		loadBtn.setForeground(NavScreen.btnFG());
		loadBtn.setBackground(NavScreen.btnBG());
		
		JButton newBtn = new JButton("Save");
		newBtn.setFont(NavScreen.text());
		newBtn.setForeground(NavScreen.btnFG());
		newBtn.setBackground(NavScreen.btnBG());
		
		//Add listeners and put buttons in layout
		backBtn.addActionListener(settingsCtrl.getCtrl(ControlType.BUTTONCONTROL));  //To provide unsaved changes dialog
		loadBtn.addActionListener(settingsCtrl.getCtrl(ControlType.BUTTONCONTROL));
		newBtn.addActionListener(settingsCtrl.getCtrl(ControlType.BUTTONCONTROL));
		subBottomPanel.add(backBtn);
		subBottomPanel.add(loadBtn);
		subBottomPanel.add(newBtn);
		
		return bottomPanel;
	}
	
	/**
	 * Creates a tab ready for insertion in a JTabbedPane. 
	 * Contains a number of cards based on what category it represents.
	 * @param category "Roads", "Nature" or "Urban"
	 * @return A tab with settings for the given category. 
	 */
	private JPanel createTab(String category) {
		JPanel tab = new JPanel();
		tab.setLayout(new BoxLayout(tab, BoxLayout.PAGE_AXIS));
		tab.setBorder(new EmptyBorder(10, 10, 10, 10));
		tab.setBackground(NavScreen.panelBG());
		createCards(category, tab); //Creates the content 
		return tab;
	}
	
	/**
	 * Creates a card with a button to reflect, change color, as well
	 * as toggling of visibility. Each card is paired with ConfigType.
	 * Designed to populate a category tab.
	 * @param type The configType to enable changes on
	 * @return
	 */
	private JPanel colorCard(ConfigType type) {
		//Create card container
		JPanel card = new JPanel();
		Dimension cardSize = new Dimension(280, 35);
		card.setLayout(new GridLayout(1,0,5,5));
		card.setBackground(NavScreen.panelBG());
		card.setMinimumSize(cardSize);
		card.setPreferredSize(cardSize);
		card.setMaximumSize(cardSize);
		
		//Create color button and set its color correctly
		Color color = (settingsCtrl.config().color(type));
		JButton colorBtn = new JButton(type.toString());
		colorBtn.setBackground(color);
		colorBtn.setForeground(color);
		colorBtn.addActionListener(settingsCtrl.getCtrl(ControlType.COLORCONTROL)); //Fetch generalized controller

		//Create visibility switch
		JCheckBox visibleTgl = new JCheckBox(type.toString());
		visibleTgl.setFont(NavScreen.text());
		visibleTgl.setBackground(NavScreen.panelBG());
		visibleTgl.setSelected(settingsCtrl.config().visible(type));
		visibleTgl.addActionListener(settingsCtrl.getCtrl(ControlType.TOGGLECONTROL));  //Fetch generalized controller
		if(type == ConfigType.BACKGROUND || type == ConfigType.ROUTE) { visibleTgl.setEnabled(false); } //Special case where it shouldn't be disableable
		
		card.add(colorBtn);
		card.add(visibleTgl);
		return card;
	}
	
	/**
	 * Used to populate a category tab with cards. 
	 * Runs through a loop and continues to create and add cards.
	 * ConfigTypes left for that category. 
	 * @param category "Roads", "Nature" or "Urban"
	 * @param target The tab to put the cards in.
	 */
	public void createCards(String category, JPanel target) {
		boolean start = false; 
		boolean end = false;
		
		switch(category) {
			case "Roads":
				for(ConfigType conf : ConfigType.values()) {
					if(conf.equals(ConfigType.WATER)) {end = true;}
					if(!end) {
						target.add(colorCard(conf));
						target.add(Box.createRigidArea(new Dimension(0,10)));
					}
				}
				break;
				
			case "Nature":
				for(ConfigType conf : ConfigType.values()) {
					if(conf.equals(ConfigType.WATER)) {start = true;}
					if(conf.equals(ConfigType.BUILDING)) {end = true;}
					if(start && !end) {
						target.add(colorCard(conf));
						target.add(Box.createRigidArea(new Dimension(0,10)));
					}
				}
				break;
				
			case "Urban": 
				for(ConfigType conf : ConfigType.values()) {
					if(conf.equals(ConfigType.BUILDING)) {start = true;}
					if(start) {
						target.add(colorCard(conf));
						target.add(Box.createRigidArea(new Dimension(0,10)));
					}
				}	
				break;
			default: 
				break;
		}
	}
	
	/**
	 * Updates Tabs by removing them and recreating them with a new config.
	 */
	public void redrawTabs() {
		tabPane.removeAll();
		tabPane.add("Roads", createTab("Roads"));
		tabPane.add("Nature", createTab("Nature"));
		tabPane.add("Urban", createTab("Urban"));
		tabPane.updateUI();
	}
	
	/**
	 * Updates whole UI, whenever the selected config changes. 
	 * Issues calls to redrawTabs and redrawMap to make UI reflect the current config.
	 */
	public void updateSelection() {
		navController.config((Config) configBox.getSelectedItem());
		settingsCtrl.config(navController.config());
		redrawMap();
		redrawTabs();
	}
	
	/**
	 * Recreates ConfigSelector ComboBox based on configs present in Model.
	 * Must be called whenever a config is added or deleted.
	 */
	public void updateList() {
		configBox.removeAllItems();
		for(Config con : model.config()) { configBox.addItem(con); }
		configBox.updateUI();
		updateSelection();
	}
	
	/**
	 * Changes selection to a given config by code.
	 * If there's no matching config, it ends up taking the first on the list. 
	 * @param name The name of the config that should be selected.
	 */
	public void setSelectedConfig(String name) {
		for(int i = 0; i < amountOfConfigs(); i++) {
			if(configBox.getItemAt(i).name().equals(name)) {
				configBox.setSelectedIndex(i);
			}
		}
		configBox.updateUI();
		updateSelection();
	}
	
	public void selectedConfig(int index) {
		configBox.setSelectedItem(index);
		configBox.updateUI();
		updateSelection();
	}

	/**
	 * Returns the category of the tabbed pane.
	 * @return
     */
	public String category() { return tabPane.getTitleAt(tabPane.getSelectedIndex()); }

	/**
	 * Returns the selected config.
	 * @return
     */
	public Config selectedConfig() { return (Config) configBox.getSelectedItem(); }

	/**
	 * Returns the amount of configs.
	 * @return
     */
	public int amountOfConfigs() { return configBox.getItemCount(); }

	/**
	 * Updates the model.
	 */
	public void redrawMap() { model.update(); }

	/**
	 * Returns the back button from the settingsPane.
	 * @return
     */
	public JButton backBtn() { return backBtn; }

	/**
	 * Returns the JComboBox containing the configs.
	 * @return
     */
	public JComboBox<Config> configBox() { return configBox; }
}
