package Model;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.ZipInputStream;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * The model is used to store and manage all information related to the map.
 */
public class Model extends Observable implements Serializable {
	private static final long serialVersionUID = 16052016L;
	public static final int METER_CONVERSION = 111323;
	
	//Primary information
	private Address[] addresses;
	private POI[] cities;
	private DataTree dataBuilding, dataCoast, dataCycle, dataFarm, dataForest, dataGrass, dataIndustrial, dataParking,
			dataPlain, dataRailway, dataResidential, dataNavigation, dataRoad_lvl1, dataRoad_lvl2, dataRoad_lvl3, dataRoad_lvl4,
			dataRoad_lvl5, dataSand, dataWalk, dataWater, dataWaterWay;
	
	//Secondary information, special cases
	private List<POI> poi, userPOI;
	private List<Config> configs;
	private List<PolygonApprox> airportWays;
	private List<PolygonApprox> parkWalls;
	
	//Map information
	private float minLat, maxLat, minLon, maxLon;
	
	//Path-related variables
	private PathFinder pathFinder;
	private Road[] path;
	private Point2D pathFrom, pathTo, highlight;


	/**
	 * Default constructor for the model.
	 */
	public Model() {
		airportWays = new ArrayList<>();
		configs = new ArrayList<>();
		parkWalls = new ArrayList<>();
		poi = new ArrayList<>();
		userPOI = new ArrayList<>();

		path = new Road[0];
		pathFrom = null;
		pathTo = null;
		highlight = null;
		loadConfigs();
	}


	/**
	 * Loads the given file OSM and stores it in this model object.
	 * @param file Path to an OSM file expected.
	 * @throws IOException Throws IOException if anything goes wrong.
	 */
	public void loadOSM(File file) throws IOException{
		InputSource in = null;
		if(file.getName().toLowerCase().endsWith(".osm")) {
			in = new InputSource(file.toURI().toString());
		} else if (file.getName().toLowerCase().endsWith(".zip")){
			ZipInputStream input = new ZipInputStream(new FileInputStream(file));
			if(input.getNextEntry().getName().endsWith(".osm")) {
				in = new InputSource(input);
			}
		}

		if(in == null) {
			throw new IOException("Unsupported file-type");
		}

		try {
			XMLReader reader = XMLReaderFactory.createXMLReader();
			reader.setContentHandler(new OSMHandler(this));
			reader.parse(in);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e);
		}
		System.gc();
	}

	/**
	 * Flags that the model has updated, effectively redrawing the map.
	 */
	public void update() {
		setChanged();
		notifyObservers();
	}

	/**
	 * Saves the model as an .obj file. Be patient, the files can be quite large.
	 * @param file Path to where the file should be saved.
	 * @throws IOException Throws IOException if anything goes wrong.
	 */
	public void save(File file) throws IOException {
		if(file == null) {throw new IOException("File object is null");}
		try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
			out.writeObject(this);
		} catch (IOException e) {
			throw e;
		}
	}

	/**
	 * Loads an Model object.
	 * @param file Path to the .obj file.
	 * @return A Model object ready for action.
	 * @throws IOException Throws IOException if anything goes wrong.
	 */
	public static Model load(File file) throws IOException {
		if(file == null) {throw new IOException("File object is null");}
		try(ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
			Model newModel = (Model) in.readObject();
			return newModel;
		} catch (IOException | ClassCastException | ClassNotFoundException | IllegalStateException ex) {
			ex.printStackTrace();
			throw new IOException("Something went wrong during loading");
		}
	}

	/**
	 * Loads a Model from an Inputstream
	 * @param input The Inputstream containing the desired Model
	 * @return The loaded Model
	 * @throws IOException Is throw if anyone goes wrong during loading
     */
	public static Model load(InputStream input) throws IOException {
		try(ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(input))) {
			Model newModel = (Model) in.readObject();
			return newModel;
		} catch (IOException | ClassCastException | ClassNotFoundException | IllegalStateException ex) {
			ex.printStackTrace();
			throw new IOException("Something went wrong during loading");
		}
	}

	/**
	 *  Loads config files containing styling. Based on:
	 */
	public void loadConfigs() {
		configs = new ArrayList<>();
		configs.add(new Config("Default"));
		for(int i = 1; i < 4; i++) {
			Config con = new Config("Temp");
			con.setDefault(i);
			configs.add(con);
		}
		update();
	}

	/**
	 * Resets the config list to only contain the standard config
	 */
	public void revertConfigs() {
		configs.remove(0);
		configs.add(0, new Config("Default"));
		configs.get(1).setDefault(1);
		configs.get(2).setDefault(2);
		configs.get(3).setDefault(3);
		update();
	}
	
	/**
	 * Removes a config object from the list of Configs.
	 * @param config Config expected to be in list. 
	 */
	public void removeConfig(Config config) { if(config != null) { configs.remove(config); } }

	/**
	 * Returns the closest road within 225meters, if any. Can return null.
	 * @param point The point to compare the distance against.
	 * @return The closest road or null.
	 * @throws NullPointerException
	 */
	public Road closestRoad(Point2D point) {
		return (Road) dataNavigation.closestToPoint(point);
	}

	/**
	 * Returns true if a path is currently stored in the model.
	 * @return
	 */
	public boolean pathExist() {
		if(path != null && path.length > 0) return true;
		return false;
	}

	/**
	 * Removes all path related objects from the model.
	 */
	public void resetPath() {
		path = null;
		pathFrom = null;
		pathTo = null;
		highlight = null;
		update();
	}

	//Getters

	/**
	 * Returns the bounds of the model.
	 * @return
     */
	public Shape bounds() {return new Rectangle2D.Float(minLon, maxLat, maxLon-minLon, minLat-maxLat);}

	/**
	 * Returns the minimum latitude of the model.
	 * @return
     */
	public float minLat() {return minLat;}

	/**
	 * Returns the maximum latitude of the model.
	 * @return
     */
	public float maxLat() {return maxLat;}

	/**
	 * Returns the minimum longitude of the model.
	 * @return
     */
	public float minLon() {return minLon;}

	/**
	 * Returns the maximum longitude of the model.
	 * @return
     */
	public float maxLon() {return maxLon;}

	/**
	 * Returns a list of points of interests (POI).
	 * @return
     */
	public List<POI> POI() {return poi;}

	/**
	 * Returns a list of the user points of interests (UserPOI).
	 * @return
     */
	public List<POI> userPOI() {return userPOI;}

	/**
	 * Returns an array of the points of interests depicting cities.
	 * @return
     */
	public POI[] cities() {return cities;}


	//List of arrays from the data tree.

	/**
	 * Returns a list of arrays of residential areas inside the given rectangle.
	 * @param bb
	 * @return
     */
	public List<PolygonApprox[]> dataResidential(Rectangle2D bb) {return dataResidential.tree(bb);}

	/**
	 * Returns a list of arrays of buildings inside the given rectangle.
	 * @param bb
	 * @return
     */
	public List<PolygonApprox[]> dataBuilding(Rectangle2D bb) {return dataBuilding.tree(bb);}

	/**
	 * Returns a list of arrays of coastlines inside the given rectangle.
	 * @param bb
	 * @return
     */
	public List<PolygonApprox[]> dataCoast(Rectangle2D bb) { return dataCoast.tree(bb);}

	/**
	 * Returns a list of arrays of cycleways inside the given rectangle.
	 * @param bb
	 * @return
     */
	public List<PolygonApprox[]> dataCycle(Rectangle2D bb) {return dataCycle.tree(bb);}

	/**
	 * Returns a list of arrays of farmland areas inside the given rectangle.
	 * @param bb
	 * @return
     */
	public List<PolygonApprox[]> dataFarm(Rectangle2D bb) {return dataFarm.tree(bb);}

	/**
	 * Returns a list of arrays of forests inside the given rectangle.
	 * @param bb
	 * @return
     */
	public List<PolygonApprox[]> dataForest(Rectangle2D bb) {return dataForest.tree(bb);}

	/**
	 * Returns a list of arrays of grass inside the given rectangle.
	 * @param bb
	 * @return
     */
	public List<PolygonApprox[]> dataGrass(Rectangle2D bb) {return dataGrass.tree(bb);}

	/**
	 * Returns a list of arrays of industrial areas inside the given rectangle.
	 * @param bb
	 * @return
     */
	public List<PolygonApprox[]> dataIndustrial(Rectangle2D bb) {return dataIndustrial.tree(bb);}

	/**
	 * Returns a list of arrays of parking areas inside the given rectangle.
	 * @param bb
	 * @return
     */
	public List<PolygonApprox[]> dataParking(Rectangle2D bb) {return dataParking.tree(bb);}

	/**
	 * Returns a list of arrays of plain areas inside the given rectangle.
	 * @param bb
	 * @return
     */
	public List<PolygonApprox[]> dataPlain(Rectangle2D bb) {return dataPlain.tree(bb);}

	/**
	 * Returns a list of arrays of railway inside the given rectangle.
	 * @param bb
	 * @return
     */
	public List<PolygonApprox[]> dataRailway(Rectangle2D bb) {return dataRailway.tree(bb);}

	/**
	 * Returns a list of arrays of navigation routes inside the given rectangle.
	 * @param bb
	 * @return
     */
	public List<PolygonApprox[]> dataNavigation(Rectangle2D bb) {return dataNavigation.tree(bb);}

	/**
	 * Returns a list of arrays of roads from first level inside the given rectangle.
	 * @param bb
	 * @return
     */
	public List<PolygonApprox[]> dataRoad_lvl1(Rectangle2D bb) {return dataRoad_lvl1.tree(bb);}

	/**
	 * Returns a list of arrays of roads from second level inside the given rectangle.
	 * @param bb
	 * @return
     */
	public List<PolygonApprox[]> dataRoad_lvl2(Rectangle2D bb) {return dataRoad_lvl2.tree(bb);}

	/**
	 * Returns a list of arrays of roads from third level inside the given rectangle.
	 * @param bb
	 * @return
     */
	public List<PolygonApprox[]> dataRoad_lvl3(Rectangle2D bb) {return dataRoad_lvl3.tree(bb);}

	/**
	 * Returns a list of arrays of roads from fourth level inside the given rectangle.
	 * @param bb
	 * @return
     */
	public List<PolygonApprox[]> dataRoad_lvl4(Rectangle2D bb) {return dataRoad_lvl4.tree(bb);}

	/**
	 * Returns a list of arrays of roads from fifth level inside the given rectangle.
	 * @param bb
	 * @return
     */
	public List<PolygonApprox[]> dataRoad_lvl5(Rectangle2D bb) {return dataRoad_lvl5.tree(bb);}

	/**
	 * Returns a list of arrays of sand areas inside the given rectangle.
	 * @param bb
	 * @return
     */
	public List<PolygonApprox[]> dataSand(Rectangle2D bb) {return dataSand.tree(bb);}

	/**
	 * Returns a list of arrays of walkways inside the given rectangle.
	 * @param bb
	 * @return
     */
	public List<PolygonApprox[]> dataWalk(Rectangle2D bb) {return dataWalk.tree(bb);}

	/**
	 * Returns a list of arrays of water areas inside the given rectangle.
	 * @param bb
	 * @return
     */
	public List<PolygonApprox[]> dataWater(Rectangle2D bb) {return dataWater.tree(bb);}

	/**
	 * Returns a list of arrays of water ways inside the given rectangle.
	 * @param bb
	 * @return
     */
	public List<PolygonApprox[]> dataWaterway(Rectangle2D bb) {return dataWaterWay.tree(bb);}

	/**
	 * Returns a list of configs.
	 * @return
     */
	public List<Config> config() {return configs;}

	/**
	 * Returns the PathFinder.
	 * @return
     */
	public PathFinder pathFinder() {return pathFinder;}

	/**
	 * Returns the path as a Road[].
	 * @return
     */
	public Road[] path() {return path;}

	/**
	 * Returns the first point of the path.
	 * @return
     */
	public Point2D pathFrom() {return pathFrom;}

	/**
	 * Returns the last point of the path.
	 * @return
     */
	public Point2D pathTo() {return pathTo;}

	/**
	 * Returns the point which depicts the fact that we have drag and drop.
	 * @return
     */
	public Point2D highlight() {return highlight;}

	//Set

	/**
	 * Sets the bounds of the model.
	 * @param minLat Minimum latitude.
	 * @param maxLat Maximum latitude.
	 * @param minLon Minimum longitude.
     * @param maxLon Maximum longitude.
     */
	public void bounds(float minLat, float maxLat, float minLon, float maxLon) {this.minLat = minLat; this.maxLat = maxLat; this.minLon = minLon; this.maxLon = maxLon;}

	/**
	 * Adds a point of interest (POI).
	 * @param point Point of interest.
     */
	public void POI(POI point) {poi.add(point);}

	/**
	 * Adds a user point of interest (UserPOI).
	 * @param point Point of interest.
     */
	public void userPOI(POI point) {userPOI.add(point);}

	/**
	 * Removes a user point of interest (UserPOI).
	 * @param point Point of interest.
     */
	public void deleteUserPOI(POI point) {userPOI.remove(point);}

	/**
	 * Sets the DataTree representing residential areas.
	 * @param dt The given DataTree.
     */
	public void dataResidential(DataTree dt) {dataResidential = dt;}

	/**
	 * Sets the DataTree representing buildings.
	 * @param dt The given DataTree.
     */
	public void dataBuilding(DataTree dt) {dataBuilding = dt;}

	/**
	 * Sets the DataTree representing coast lines.
	 * @param dt The given DataTree.
     */
	public void dataCoast(DataTree dt) {dataCoast = dt;}

	/**
	 * Sets the DataTree representing cycle ways.
	 * @param dt The given DataTree.
     */
	public void dataCycle(DataTree dt) {dataCycle = dt;}

	/**
	 * Sets the DataTree representing farmland areas.
	 * @param dt The given DataTree.
     */
	public void dataFarm(DataTree dt) {dataFarm = dt;}

	/**
	 * Sets the DataTree representing forests.
	 * @param dt The given DataTree.
     */
	public void dataForest(DataTree dt) {dataForest = dt;}

	/**
	 * Sets the DataTree representing grass areas.
	 * @param dt The given DataTree.
     */
	public void dataGrass(DataTree dt) {dataGrass = dt;}

	/**
	 * Sets the DataTree representing industrial areas.
	 * @param dt The given DataTree.
     */
	public void dataIndustrial(DataTree dt) {dataIndustrial = dt;}

	/**
	 * Sets the DataTree representing parking areas.
	 * @param dt The given DataTree.
     */
	public void dataParking(DataTree dt) {dataParking = dt;}

	/**
	 * Sets the DataTree representing plain areas.
	 * @param dt The given DataTree.
     */
	public void dataPlain(DataTree dt) {dataPlain = dt;}

	/**
	 * Sets the DataTree representing rail ways.
	 * @param dt The given DataTree.
     */
	public void dataRailway(DataTree dt) {dataRailway = dt;}

	/**
	 * Sets the DataTree representing navigation routes.
	 * @param dt The given DataTree.
     */
	public void dataNavigation(DataTree dt) {dataNavigation = dt;}

	/**
	 * Sets the DataTree representing level one roads.
	 * @param dt The given DataTree.
     */
	public void dataRoad_lvl1(DataTree dt) {dataRoad_lvl1 = dt;}

	/**
	 * Sets the DataTree representing level two roads.
	 * @param dt The given DataTree.
     */
	public void dataRoad_lvl2(DataTree dt) {dataRoad_lvl2 = dt;}

	/**
	 * Sets the DataTree representing level three roads.
	 * @param dt The given DataTree.
     */
	public void dataRoad_lvl3(DataTree dt) {dataRoad_lvl3 = dt;}

	/**
	 * Sets the DataTree representing level four roads.
	 * @param dt The given DataTree.
     */
	public void dataRoad_lvl4(DataTree dt) {dataRoad_lvl4 = dt;}

	/**
	 * Sets the DataTree representing level five roads.
	 * @param dt The given DataTree.
     */
	public void dataRoad_lvl5(DataTree dt) {dataRoad_lvl5 = dt;}

	/**
	 * Sets the DataTree representing sand areas.
	 * @param dt The given DataTree.
     */
	public void dataSand(DataTree dt) {dataSand = dt;}

	/**
	 * Sets the DataTree representing walk ways.
	 * @param dt The given DataTree.
     */
	public void dataWalk(DataTree dt) {dataWalk = dt;}

	/**
	 * Sets the DataTree representing water areas.
	 * @param dt The given DataTree.
     */
	public void dataWater(DataTree dt) {dataWater = dt;}

	/**
	 * Sets the DataTree representing water ways.
	 * @param dt The given DataTree.
     */
	public void dataWaterway(DataTree dt) {dataWaterWay = dt;}

	/**
	 * Adds a config.
	 * @param conf
     */
	public void config(Config conf) {configs.add(conf);}

	/**
	 * Sets the PathFinder.
	 * @param pf
     */
	public void pathFinder(PathFinder pf) {pathFinder = pf;}

	/**
	 * Sets the path to a given Road[] and resets a potential highlighted point.
	 * @param ra
     */
	public void path(Road[] ra) {
		path = ra;
		highlight = null;
	}

	/**
	 * Sets the first point of the path from a Vertex object position.
	 * @param ve
     */
	public void pathFrom(Vertex ve) {pathFrom = ve.getPosition();}

	/**
	 * Sets the last point of the path from a Vertex objects position.
	 * @param ve
     */
	public void pathTo(Vertex ve) {pathTo = ve.getPosition();}

	/**
	 * Sets the first point of the path from a Point2D.
	 * @param p
     */
	public void pathFrom(Point2D p) {pathFrom = p;}

	/**
	 * Sets the last point of the path from a Point2D.
	 * @param p
     */
	public void pathTo(Point2D p) {pathTo = p;}

	/**
	 * Sets the highlighted point with a Point2D used to depict drag and drop.
	 * @param p
     */
	public void highlight(Point2D p) {highlight = p;}
	
	//Special Cases, get/set

	/**
	 * Adds all addresses to the model.
	 * @param points
     */
	public void address(Address[] points) {addresses = points;}

	/**
	 * Adds a way to a list of PolygonApprox.
	 * @param way
     */
	public void airport(PolygonApprox way){ airportWays.add(way);}

	/**
	 * Adds all city points of interest to the model.
	 * @param points
     */
	public void cities(POI[] points) {cities = points;}

	/**
	 * Adds a way to the list of PolygonApprox.
	 * @param way
     */
	public void parkWall(PolygonApprox way) {parkWalls.add(way);}

	/**
	 * Returns all of the addresses form the sorted set of addresses as an Address[].
	 * @return
     */
	public Address[] addresses() {return addresses;}

	/**
	 * Returns a list of PolygonApprox depicting airport ways.
	 * @return
     */
	public List<PolygonApprox> airport() {return airportWays;}

	/**
	 * Returns a list of PolygonApprox depicting park walls.
	 * @return
     */
	public List<PolygonApprox> parkWall(){return parkWalls;}
}