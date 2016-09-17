package Controller;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;
import Model.Model;
import View.LoadingScreen;

/**
 * Main controller of the Kronhjort Inc. map.
 */
public class Main {
	private static Model model;
	private static NavController navController;

	/**
	 * The recommended way to start the program.
	 * @param kronhjort None expected.
     */
	public static void main(String[] kronhjort) {
		new Main();
	}
	
	/**
	 * Only for test purposes! Never used by the program.
	 * @param modelTest True will create an empty model. False will leave the model as null.
	 */
	public Main(boolean modelTest) {
		if(modelTest) { model = new Model(); }
	}
	
	/**
	 * Initializes program. Starts call sequence to load default model and setup GUI. 
	 */
	public Main(){
		GUI();
		model = new Model();
		
		InputStream in = Main.class.getClassLoader().getResourceAsStream("denmark.obj");
		if(in == null) {
			JOptionPane.showMessageDialog(null, "Loading of default map failed\nPlease manually select a compatible .osm, .obj or .zip file");
			loadFile(openFile());
		} else {
			LoadingScreen ls = new LoadingScreen("Initializing default map...");
			try {
				model = Model.load(in);
				ls.dispose();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Loading of default map failed\nPlease manually select a compatible .osm, .obj or .zip file");
				ls.dispose();
				loadFile(openFile());
			}
		}
		navController = new NavController(1280, 720);
	}

	/**
	 * Returns the current Model used be the program
	 * @return Model The Model currently in use by the program
     */
	public static Model model() {
		return model;
	}
	
	/**
	 * Loads a new Model from an .osm, .zip or .obj file
	 * Reverts back to the last Model if the load fails
	 * @param file The file of the new desired Model
     */
	public static void loadNewModel(File file){
		Model oldModel = model;
		int oldW = navController.screenWidth();
		int oldH = navController.screenHeight();
		Point oldLocation = navController.screenLocation();
		navController.dispose();
		if(file.getName().endsWith(".zip") || file.getName().endsWith(".osm")) {
			model = new Model();
			try { model.loadOSM(file); }
			catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Encountered a problem during loading\nThe last map will be reloaded.", "Load Failed", JOptionPane.ERROR_MESSAGE);
				model = oldModel;
			}
		} else if (file.getName().endsWith(".obj")){
			LoadingScreen ls = new LoadingScreen("Loading model...");
			try {
				model = Model.load(file);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "This is not a supported model type.\nObject might be of a wrong file type."
						+ "\nLast map will be reloaded.", "Load Failed", JOptionPane.ERROR_MESSAGE);
				model = oldModel;
			}
			ls.dispose();
		} else {
			JOptionPane.showMessageDialog(null, "File could not be read.\nPlease use the following formats:\n"
					+ ".osm, .zip containing .osm or .obj. \nLast map will be reloaded.", "Unsupported File", JOptionPane.ERROR_MESSAGE);
		}
		navController = new NavController(oldW, oldH);
		navController.screenLocation(oldLocation);
	}

	/**
	 * Shows a filechooser dialog that allows selection of compatible map files.
	 * @return File selected in dialog box. Null if user canceled
	 */
	private File openFile() {
		JFileChooser choose = new JFileChooser();
		choose.setFileFilter(new FileNameExtensionFilter("OSM-files (*.osm; *.zip; *.obj)", "osm", "zip", "obj"));
		int option = choose.showOpenDialog(null);
		if(option == JFileChooser.APPROVE_OPTION) {
			return choose.getSelectedFile();
		}
		else {return null;}
	}
	
	/**
	 * Takes a compatible map file and loads into model.
	 * @param file Compatible map file. 
	 */
	private void loadFile(File file) {
		try {
			if (file.getName().endsWith(".zip") || file.getName().endsWith(".osm")) {
				model.loadOSM(file);
			} else if (file.getName().endsWith(".obj")) {
				model = Model.load(file);
			} else {
				JOptionPane.showMessageDialog(null, "Wrong file format. Choose another file.", "Wrong file error", JOptionPane.ERROR_MESSAGE);
				loadFile(openFile());
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Something went wrong, try again.", "Unknown error", JOptionPane.ERROR_MESSAGE);
			loadFile(openFile());
		}
		
	}
	
	/**
	 * Sets system look and feel for cross platform compatability
	 */
	private void GUI() {
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException ex) {
			JOptionPane.showMessageDialog(null, "Setting up the Graphical User Interface failed.\nMap might look somewhat off.", "Style error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Returns the current navController object.
	 * @return
     */
	public static NavController navController() {
		return navController;
	}
}
