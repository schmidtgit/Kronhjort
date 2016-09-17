package Model;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import enums.ConfigType;

/**
 * Used for coloring and visibility of map elements in the program.
 */
public class Config implements Serializable {
	private static final long serialVersionUID = 16052016L;
	String name;
	boolean standard;
	boolean water, farm, motorway, bigroad, smallroad, residential, airway, railway, sand,
	walkway, cycle, building, forest, plain, grass, industrial, parking;
	Color waterColor, backgroundColor, farmColor, motorwayColor, bigroadColor, smallroadColor, residentialColor, airwayColor, railwayColor, sandColor,
	walkwayColor, cycleColor, buildingColor, forestColor, plainColor, grassColor, industrialColor, parkingColor, routeColor;

	/**
	 * Creates a config with standard values.
	 * @param name The desired name of the Config object.
     */
	public Config(String name) {
		this.name = name;
		showAll();
		waterColor = new Color(124, 174, 231);
		backgroundColor = new Color(235, 225, 220);
		//Road colors
		motorwayColor = new Color(255, 128, 0);
		bigroadColor = new Color(227, 158, 152);
		smallroadColor = Color.WHITE;
		residentialColor = new Color(213, 204, 199);
		airwayColor = new Color(192, 192, 192);
		railwayColor = new Color(71, 92, 127);
		walkwayColor = new Color(118, 136, 139);
		cycleColor = new Color(189, 94, 89);
		routeColor = Color.RED;
		//Building color
		buildingColor = new Color(172, 172, 172);
		//Area colors
		forestColor = new Color(114, 172, 102);
		plainColor = new Color(232, 221, 169);
		grassColor = new Color(170, 203, 159);
		industrialColor = new Color(220, 200, 166);
		parkingColor = new Color(197, 196, 222);
		farmColor = new Color(233, 206, 161);
		sandColor = new Color(210, 204, 187);
	}

	/**
	 * Loads a Config using a filename.
	 * @param filename The filename of the desired config.
	 * @return The loaded config.
	 * @throws IOException If something fails during load an IOException is thrown.
     */
	public static Config load(String filename) throws IOException{
		try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
			Config c = (Config) in.readObject();
			return c;
		} catch (IOException | ClassNotFoundException e) {
			throw new IOException("Error loading file");
		}
	}
	
	public static Config load(File file) throws IOException{
		try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
			Config c = (Config) in.readObject();
			return c;
		} catch (IOException | ClassNotFoundException e) {
			throw new IOException("Error loading file");
		}
	}

	/**
	 * Saves a config to the desired destination.
	 * @param filename The desired location of the file.
	 * @throws IOException If something fails during load an IOException is thrown.
     */
	public void save(String filename) throws IOException{
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
			out.writeObject(this);
		} catch (IOException e) {
			throw new IOException("Error saving file");
		}
	}
	
	public void save(File file) throws IOException {
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
			out.writeObject(this);
		} catch (IOException e) {
			throw new IOException("Error saving file");
		}
	}
	
	/**
	 * Sets the map colors to one of the default colorschemes.
	 * @param i
	 */
	public void setDefault(int i) {
		showAll();
		switch(i) {
			case 1:
				// Google
				name = "Google-like";
				standard = true;
				motorwayColor = new Color(250, 158, 37);
				bigroadColor = new Color(255, 225, 104);
				smallroadColor = new Color(255, 255, 255);
				airwayColor = new Color(211, 202, 189);
				railwayColor = new Color(197, 197, 196);
				cycleColor = new Color(196, 192, 180);
				walkwayColor = new Color(196, 192, 180);
				routeColor = new Color(66, 133, 244);
				waterColor = new Color(176, 211, 255);
				farmColor = new Color(255,255,255);
				farm = false;
				backgroundColor = new Color(240, 237, 229);
				forestColor = new Color(210, 228, 200);
				plainColor =  new Color(210, 228, 200);
				grassColor = new Color(202, 223, 170);
				sandColor = new Color(250, 242, 199);
				buildingColor = new Color(244, 243, 236);
				industrialColor = new Color(233, 229, 220);
				residentialColor =  new Color(233, 229, 220);
				parkingColor =  new Color(225, 225, 225);
				parking = false;
				break;
			case 2:
				// Krak
				name = "Krak-like";
				standard = true;
				motorwayColor = new Color(204, 43, 43);
				bigroadColor = new Color(248, 217, 55);
				smallroadColor = new Color(255, 255, 255);
				airwayColor = new Color(201, 201, 200);
				railwayColor = new Color(155, 155, 155);
				cycleColor = new Color(255, 255, 255);
				cycle = false;
				walkwayColor = new Color(255, 255, 255);
				walkway = false;
				routeColor = new Color(104, 1, 154);
				waterColor = new Color(108, 189, 243);
				farmColor = new Color(255,255,255);
				farm = false;
				backgroundColor = new Color(255, 255, 204);
				forestColor = new Color(119, 215, 81);
				plainColor =  new Color(144, 238, 144);
				grassColor = new Color(144, 238, 144);
				sandColor = new Color(255, 255, 204);
				buildingColor = new Color(235, 175, 89);
				industrialColor = new Color(211, 211, 211);
				residentialColor =  new Color(244, 213, 66);
				parkingColor =  new Color(225, 225, 225);
				parking = false;
				break;
			case 3:
				// OSM
				name = "OSM-like";
				standard = true;
				motorwayColor = new Color(160, 152, 176);
				bigroadColor = new Color(211, 149, 144);
				smallroadColor = new Color(223, 219, 199);
				airwayColor = new Color(223, 219, 199);
				railwayColor = new Color(165, 165, 165);
				cycleColor = new Color(223, 219, 199);
				walkwayColor = new Color(223, 219, 199);
				routeColor = new Color(190, 0, 5);
				waterColor = new Color(144, 204, 202);
				farmColor = new Color(221,221,190);
				backgroundColor = new Color(255, 245, 240);
				forestColor = new Color(178, 194, 157);
				plainColor =  new Color(208, 219, 172);
				grassColor = new Color(208, 219, 172);
				sandColor = new Color(237, 228, 178);
				buildingColor = new Color(220, 214, 214);
				industrialColor = new Color(215, 200, 202);
				residentialColor =  new Color(240, 238, 228);
				parkingColor =  new Color(240, 238, 228);
				break;
		}
	}
	
	/**
	 * Creates a copy of this configs color and visiblity settings and returns it as a new object.
	 * @return New Config with same settings
	 */
	public Config copy() {
		Config copy = new Config("Copy");
		standard = false;
		//Sync Colors
		copy.color(ConfigType.MOTORWAY, motorwayColor);
		copy.color(ConfigType.BIGROADS, bigroadColor);
		copy.color(ConfigType.SMALLROADS, smallroadColor);
		copy.color(ConfigType.AIRWAY, airwayColor);
		copy.color(ConfigType.RAILWAY, railwayColor);
		copy.color(ConfigType.CYCLE, cycleColor);
		copy.color(ConfigType.WALKWAY, walkwayColor);
		copy.color(ConfigType.ROUTE, routeColor);
		copy.color(ConfigType.WATER, waterColor);
		copy.color(ConfigType.FARMLAND, farmColor);
		copy.color(ConfigType.BACKGROUND, backgroundColor);
		copy.color(ConfigType.FOREST, forestColor);
		copy.color(ConfigType.PLAIN, plainColor);
		copy.color(ConfigType.GRASS, grassColor);
		copy.color(ConfigType.SAND, sandColor);
		copy.color(ConfigType.BUILDING, buildingColor);
		copy.color(ConfigType.INDUSTRIAL, industrialColor);
		copy.color(ConfigType.RESIDENTIAL, residentialColor);
		copy.color(ConfigType.PARKING, parkingColor);
		//Sync visibility
		if(copy.visible(ConfigType.MOTORWAY) != motorway) copy.changeState(ConfigType.MOTORWAY);
		if(copy.visible(ConfigType.BIGROADS) != bigroad) copy.changeState(ConfigType.BIGROADS);
		if(copy.visible(ConfigType.SMALLROADS) != smallroad) copy.changeState(ConfigType.SMALLROADS);
		if(copy.visible(ConfigType.AIRWAY) != airway) copy.changeState(ConfigType.AIRWAY);
		if(copy.visible(ConfigType.RAILWAY) != railway) copy.changeState(ConfigType.RAILWAY);
		if(copy.visible(ConfigType.CYCLE) != cycle) copy.changeState(ConfigType.CYCLE);
		if(copy.visible(ConfigType.WALKWAY) != walkway) copy.changeState(ConfigType.WALKWAY);
		if(copy.visible(ConfigType.WATER) != water) copy.changeState(ConfigType.WATER);
		if(copy.visible(ConfigType.FARMLAND) != farm) copy.changeState(ConfigType.FARMLAND);
		if(copy.visible(ConfigType.FOREST) != forest) copy.changeState(ConfigType.FOREST);
		if(copy.visible(ConfigType.PLAIN) != plain) copy.changeState(ConfigType.PLAIN); 
		if(copy.visible(ConfigType.GRASS) != grass) copy.changeState(ConfigType.GRASS);
		if(copy.visible(ConfigType.SAND) != sand) copy.changeState(ConfigType.SAND);
		if(copy.visible(ConfigType.BUILDING) != building) copy.changeState(ConfigType.BUILDING);
		if(copy.visible(ConfigType.INDUSTRIAL) != industrial) copy.changeState(ConfigType.INDUSTRIAL);
		if(copy.visible(ConfigType.RESIDENTIAL) != residential) copy.changeState(ConfigType.RESIDENTIAL);
		if(copy.visible(ConfigType.PARKING) != parking) copy.changeState(ConfigType.PARKING);
		return copy;
	}

	/**
	 * Returns the name of the config.
	 * @return The name of the config.
     */
	public String toString(){ return name; }

	/**
	 * Enables all objects that can be drawn.
	 */
	private void showAll(){
		water = true;
		farm = true; 
		motorway = true;
		bigroad = true;
		smallroad = true;
		residential = true;
		airway = true;
		railway = true;
		walkway = true;
		cycle = true;
		building = true;
		forest = true;
		plain = true;
		grass = true;
		industrial = true;
		parking = true;
		sand = true;
	}

	/**
	 * Retrieves the color of a certain object.
	 * @param cc The ConfigType of the desired object.
	 * @return The color corresponding to the ConfigType.
     */
	public Color color(ConfigType cc){
		switch(cc){
			case AIRWAY:
				return airwayColor;
			case BACKGROUND:
				return backgroundColor;
			case BUILDING:
				return buildingColor;
			case CYCLE:
				return cycleColor;
			case FARMLAND:
				return farmColor;
			case FOREST:
				return forestColor;
			case GRASS:
				return grassColor;
			case MOTORWAY:
				return motorwayColor;
			case BIGROADS:
				return bigroadColor;
			case SMALLROADS:
				return smallroadColor;
			case INDUSTRIAL:
				return industrialColor;
			case PARKING:
				return parkingColor;
			case PLAIN:
				return plainColor;
			case RAILWAY:
				return railwayColor;
			case RESIDENTIAL:
				return residentialColor;
			case SAND:
				return sandColor;
			case WALKWAY:
				return walkwayColor;
			case WATER:
				return waterColor;
			case ROUTE:
				return routeColor;
			default:
				return null;
		}
	}

	/**
	 * Sets the color of a certain map element.
	 * @param ct The ConfigType to update.
	 * @param c The desired color.
     */
	public void color(ConfigType ct, Color c){
		switch(ct){
			case AIRWAY:
				airwayColor = c;
				break;
			case BACKGROUND:
				backgroundColor = c;
				break;
			case BUILDING:
				buildingColor = c;
				break;
			case CYCLE:
				cycleColor = c;
				break;
			case FARMLAND:
				farmColor = c;
				break;
			case FOREST:
				forestColor = c;
				break;
			case GRASS:
				grassColor = c;
				break;
			case MOTORWAY:
				motorwayColor = c;
				break;
			case BIGROADS:
				bigroadColor = c;
				break;
			case SMALLROADS:
				smallroadColor = c;
				break;
			case INDUSTRIAL:
				industrialColor = c;
				break;
			case PARKING:
				parkingColor = c;
				break;
			case PLAIN:
				plainColor = c;
				break;
			case RAILWAY:
				railwayColor = c;
				break;
			case RESIDENTIAL:
				residentialColor = c;
				break;
			case SAND:
				sandColor = c;
				break;
			case WALKWAY:
				walkwayColor = c;
				break;
			case WATER:
				waterColor = c;
				break;
			case ROUTE:
				routeColor = c;
				break;
			default:
				break;
		}
	}

	/**
	 * Checks visibility of the ConfigType.
	 * @param ct The ConfigType to return.
	 * @return True if the map element is visible, false if not.
     */
	public boolean visible(ConfigType ct){
		switch(ct){
			case AIRWAY:
				return airway;
			case BUILDING:
				return building;
			case CYCLE:
				return cycle;
			case FARMLAND:
				return farm;
			case FOREST:
				return forest;
			case GRASS:
				return grass;
			case MOTORWAY:
				return motorway;
			case BIGROADS:
				return bigroad;
			case SMALLROADS:
				return smallroad;
			case INDUSTRIAL:
				return industrial;
			case PARKING:
				return parking;
			case PLAIN:
				return plain;
			case RAILWAY:
				return railway;
			case RESIDENTIAL:
				return residential;
			case SAND:
				return sand;
			case WALKWAY:
				return walkway;
			case WATER:
				return water;
			default:
				return true;
		}
	}

	/**
	 * Changes the visibility of a ConfigType.
	 * @param ct The desired ConfigType toggle visibility on.
     */
	public void changeState(ConfigType ct){
		switch(ct){
			case AIRWAY:
				airway = !airway;
				break;
			case BUILDING:
				building = !building;
				break;
			case CYCLE:
				cycle = !cycle;
				break;
			case FARMLAND:
				farm = !farm;
				break;
			case FOREST:
				forest = !forest;
				break;
			case GRASS:
				grass = !grass;
				break;
			case MOTORWAY:
				motorway = !motorway;
				break;
			case BIGROADS:
				bigroad = !bigroad;
				break;
			case SMALLROADS:
				smallroad = !smallroad;
				break;
			case INDUSTRIAL:
				industrial = !industrial;
				break;
			case PARKING:
				parking = !parking;
				break;
			case PLAIN:
				plain = !plain;
				break;
			case RAILWAY:
				railway = !railway;
				break;
			case RESIDENTIAL:
				residential = !residential;
				break;
			case SAND:
				sand = !sand;
				break;
			case WALKWAY:
				walkway = !walkway;
				break;
			case WATER:
				water = !water;
				break;
			default:
				break;
		}
	}

	/**
	 * Returns whether or not the Config object is a standard file and therefore unchangeable.
	 * @return True if the Config is in the standard package.
     */
	public boolean standard() {return standard;}

	/**
	 * Returns the name of the Config-object.
	 * @return The name of the Config.
     */
	public String name() { return name;}

	/**
	 * Changes the name of the Config-object.
	 * @param name The desired name of the Config.
     */
	public void name(String name) {this.name = name;}
}
