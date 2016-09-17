package Controller;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import View.LoadingScreen;
import View.SettingsPane;
import Model.Config;
import enums.ControlType;
import enums.ConfigType;

/**
 * Controller for SettingsPane.
 * Provides centralized color and toggle controllers.
 */
public class SettingsController {
	private SettingsPane panel;
	private Config config; //Temporary config in use
	private ActionListener btnCtrl, colorCtrl, toggleCtrl; //Controllers stored for very similar functions
	private boolean saved = true;  //Tracks unsaved changes
	private JFileChooser fc; //Dialog for save/load
	private int listLength; 

	/**
	 * Initializes a new SettingsController, the corresponding SettingsPane, and
	 * button listeners.
	 * @param navController The current NavController.
     */
	public SettingsController(NavController navController) {
		//Create single instances of color and toggle controllers.
		btnCtrl = btnCtrl();
		colorCtrl = colorCtrl();
		toggleCtrl = toggleCtrl();
		config = Main.model().config().get(0);
		listLength = Main.model().config().size();

		panel = new SettingsPane(this, navController);
		panel.configBox().addItemListener(configBoxCtrl(panel.configBox()));	
	}

	/**
	 * Returns the settingsPane.
	 * @return
     */
	public SettingsPane settingsPane() {return panel;}

	/**
	 * Sets the config currently used in the program.
	 * @param c
     */
	public void config(Config c) {config = c;}

	/**
	 * Returns the config.
	 * @return
     */
	public Config config() {return config;}
	
	/**
	 * Returns listener based on the type.
	 * @param type The type of controller needed for component.
	 * @return The corresponding controller type.
	 */
	public ActionListener getCtrl(ControlType type) {
		switch(type) {
			case BUTTONCONTROL:
				return btnCtrl;
			case COLORCONTROL:
				return colorCtrl;
			case TOGGLECONTROL:
				return toggleCtrl;
			default: 
				return null;
		}
	}

	/**
	 * Assigns listener to the back button in the settingsPane.
	 * @param cardSwitch listener to switch cards.
     */
	public void addBackCtrl(ActionListener cardSwitch) {
		panel.backBtn().addActionListener(cardSwitch);
	}

	/**
	 * Listener to update selection in JComboBox.
	 * @param box
	 * @return Listener to update selection.
     */
	public ItemListener configBoxCtrl(JComboBox box) {
		ItemListener listener = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {
				if(box.getItemCount() > 3) {  //To avoid indexOutOfBounds crash. (Happens during save/load)
					panel.updateSelection();
					panel.redrawTabs();
				}
			}
		};
		return listener;
	}

	/**
	 * Listener for buttons.
	 * @return Listener for buttons.
     */
	private ActionListener btnCtrl(){
		ActionListener listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String caller;
				if(e.getSource() instanceof JButton) {
					caller = ((JButton)e.getSource()).getText();
					switch(caller) {
					case "Back":
						if(!saved) { unsavedChangesPopup(); }
						break;
					case "Load":
						load();
						break;
					case "Save":
						save();			
						break;
					default: 
						break;
					}
				}			
			}
		};
		return listener;
	}

	/**
	 * Listener to change color.
	 * @return Listener to change color.
     */
	private ActionListener colorCtrl(){
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e)
            {
				if(e.getSource() instanceof JButton) {
					JButton colorBtn = (JButton) e.getSource(); 
					//Spawn colordialog box and store it's selected color upon closing it. 
		           	Color newColor = JColorChooser.showDialog(null, "Select new Color for " + colorBtn.getText(), colorBtn.getBackground());
		           	if(newColor != null) {
		           		//Lookup the corresponding enum and change it's color within the config object. 
		           		config.color(stringToEnum(colorBtn.getText()), newColor);
		           		colorBtn.setBackground(newColor); //Update button color to reflect the newly set color. 
		               	colorBtn.setForeground(newColor);
		           	}
					panel.redrawMap();
	            	saved = false; 
				}
            }
		};
		return listener;
	}

	/**
	 * Listener to toggle visibility.
	 * @return Listener to toggle visibility.
     */
	private ActionListener toggleCtrl(){
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e)
            {
				if(e.getSource() instanceof JCheckBox) {
					JCheckBox visibleTgl = (JCheckBox) e.getSource(); 
		            config.changeState(stringToEnum(visibleTgl.getText())); //Look up config-type and flip toggle.						
	            	panel.redrawMap();
	            	saved = false;
				}
            }
		};
		return listener;
	}
	
	/**
	 * Spawns a dialog box with buttons to save changes or revert.
	 */
	private void unsavedChangesPopup() {
		Object[] options = {"Save now", "Revert changes"}; //StringModel for yes/no btns.
		String msg = "Changes has not been saved.\nWould you like to save them before returning to map view?";
		String title = "Unsaved changes warning";
		int response = JOptionPane.showOptionDialog(panel, msg, title, JOptionPane.YES_NO_OPTION, 
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		if(response == JOptionPane.YES_OPTION) { 
			Boolean successful = save(); //Try to let user save
			if(!successful) { unsavedChangesPopup(); }  //If user cancels at save stage, retry
		} else {
			revert();
		}
	}
	
	/**
	 * Saves currently selected config to disk with a user supplied filename.
	 * @return True if saving succeeded, false if user aborted or saving failed.
	 */
	private boolean save() {
		fc = new JFileChooser();
		fc.setFileFilter(new FileNameExtensionFilter("Object(*.obj)", "obj"));
		fc.setDialogTitle("Select where to save settings profile:");
		if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			Config copy = config.copy();
			if(file.getName().equals(copy.name())) { copy.name(file.getName() + "_New"); } 
			else { copy.name(file.getName()); }
			if(!file.getName().endsWith(".obj")) { file = new File(file + ".obj"); }
			try {
				copy.save(file);
				Main.model().config(copy);
				revert();
				panel.updateList();
				panel.selectedConfig(listLength);
				listLength++;
				saved = true;
				return true;
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(null, "Encountered an error while saving the file.\nTry again.", "Save error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return false;
	}
	
	private void load() {
		fc = new JFileChooser();
		fc.setFileFilter(new FileNameExtensionFilter("Object(*.obj)", "obj"));
		fc.setDialogTitle("Select a compatible settings profile:");
		if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if(!file.getName().endsWith(".obj")) {
				JOptionPane.showMessageDialog(null, "The selected file is not a compatible settings profile\n"
						+ "It must be a '.obj' file.\nTry again", "Load error", JOptionPane.ERROR_MESSAGE);
			}
			try {
				Config loadConfig = Config.load(file);
				loadConfig.name(loadConfig.name().replace(".obj", ""));
				Main.model().config(loadConfig);
				panel.updateList();
				panel.selectedConfig(listLength);
				listLength++;
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(null, "Encountered an error while loading the file.\n"
						+ "It might be an outdated version\n"
						+ "Try again.", "Load error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/**
	 * Reverts current config, back to temporary saved unchanged state.
	 */
	private void revert() {
		Main.model().revertConfigs();
		int index = panel.configBox().getSelectedIndex();
		panel.updateList();
		panel.selectedConfig(index);
		saved = true; 
	}
	
	/**
	 * Returns a ConfigType corresponding to the given String.
	 * @param txt Expected to correspond to a ConfigType enum.
	 * @return The enum that should be coupled with that colorBtn. Might return null.
	 */
	private ConfigType stringToEnum(String txt) {
		for(ConfigType c : ConfigType.values()) {
			if(c.toString().equals(txt)) {return c;}
		}
		return null;
	}
}
