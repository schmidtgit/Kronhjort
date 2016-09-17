package Model;
import enums.POIType;
import enums.RoadAccess;
import enums.RoadType;
import enums.WayType;
import View.LoadingScreen;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * XML parser specialized to handle the OSM-format.
 */
public class OSMHandler extends DefaultHandler {
	// Miscellaneous
	private Model model;
	private String name;
	private Factory fac;
	private List<RoadWay> roads;
	private Way way;
	private List<List<Point2D>> relation;
	private float minLat, maxLat, minLon, maxLon, lonfactor;
	// ID fields
	private long id, wayID;
	// ID maps
	private IdMap points;
	private Map<Long, Way> ways;
	private Map<Long, Way> relMap;
	// Coast related fields
	private CoastBuilder builder;
	private List<Way> coasts;
	// Enums
	private WayType type;
	private POIType pType;
	private RoadType roadType;
	private Set<WayType> notRelationTypes;
	// Variables for addresses
	private String street, housenumber, city;
	private short postcode;
	private boolean isAddressNode;
	private float lon, lat;
	// Variables for roads.
	private short maxSpeed;
	private byte oneWay, carRestriction, bikeRestriction;
	private boolean roundabout;
	private boolean noAccess;
	// Variables for loading screen.
	private boolean isWayDetected, isRelationDetected;
	private LoadingScreen load;

	/**
	 * Initializes the OSM-Handler with the given Model.
	 * @param model The Model which holds the data.
     */
	public OSMHandler(Model model) {
		fac = new Factory();
		this.model = model;
		points = new IdMap(9973); //The largest prime number below 10000, as the symbol table can not expand.
		ways = new HashMap<>();
		roads = new ArrayList<>();
		relMap = new HashMap<>();
		builder = new CoastBuilder();
		coasts = new ArrayList<>();
		notRelationTypes = new HashSet<>();
		notRelationTypes.add(WayType.COASTLINE);
		notRelationTypes.add(WayType.PARKWALL);
		notRelationTypes.add(WayType.RAILWAY);
		notRelationTypes.add(WayType.ROAD);
		notRelationTypes.add(WayType.WALKWAY);
		notRelationTypes.add(WayType.WATERWAY);
		maxSpeed = -1;
		oneWay = 0;
		noAccess = false;
		carRestriction = -1;
		bikeRestriction = -1;

		name = "".intern();

		lon = 0;
		lat = 0;


		street = "";
		housenumber = "";
		city = "";
		postcode = 0;
		isAddressNode = false;
		
		load = new LoadingScreen("Loading Nodes...");
		isWayDetected = false;
		isRelationDetected = false;
	}

	public void startElement(String uri, String localName, String qName, Attributes atts) {
		switch (qName) {
			//Sorted according to occurrences
			case "node":
				startNode(atts);
				break;
			case "nd":
				startNd(atts);
				break;
			case "tag":
				startTag(atts);
				break;
			case "way":
				startWay(atts);
				break;
			case "member":
				startMember(atts);
				break;
			case "relation":
				startRelation();
				break;
			case "bounds":
				startBounds(atts);
				break;
			default:
				break;
		}
	}

	/**
	 * Used to set up the bounds of the map.
	 * @param atts The attributes of the bounds tag.
     */
	private void startBounds(Attributes atts){
		minLat = Float.parseFloat(atts.getValue("minlat"));
		minLon = Float.parseFloat(atts.getValue("minlon"));
		maxLat = Float.parseFloat(atts.getValue("maxlat"));
		maxLon = Float.parseFloat(atts.getValue("maxlon"));
		lonfactor = (float) Math.cos(Math.PI / 180 * (minLat + (maxLat - minLat) / 2));
		minLat = -minLat;
		maxLat = -maxLat;
		minLon *= lonfactor;
		maxLon *= lonfactor;
		model.bounds(minLat, maxLat, minLon, maxLon);
	}

	/**
	 * Used to store the node in the IDMap in model coordinates.
	 * @param atts The attributes of the node tag.
     */
	private void startNode(Attributes atts){
		pType = POIType.UNKNOWN;
		id = Long.parseLong(atts.getValue("id"));
		lat = Float.parseFloat(atts.getValue("lat"));
		lon = Float.parseFloat(atts.getValue("lon"));
		points.put(id, lon * lonfactor, -lat); //lonfactor used to flatten map.
	}

	/**
	 * Used to set up a way object.
	 * @param atts The attributes of the way tag.
     */
	private void startWay(Attributes atts){
		if (!isWayDetected) {
			load.loadingText("Loading Ways...");
			isWayDetected = true;
		}
		wayID = Long.parseLong(atts.getValue("id"));
		type = WayType.UNKNOWN;
		way = new Way();
	}

	/**
	 * Used to add a node to the current way.
	 * @param atts The attributes of the nd tag.
     */
	private void startNd(Attributes atts){
		id = Long.parseLong(atts.getValue("ref"));
		Point2D p = points.get(id);
		if(p!=null){ way.add(id);}
	}

	/**
	 * Used to set up a relation.
     */
	private void startRelation() {
		if (!isRelationDetected) {
			load.loadingText("Loading Relations...");
			isRelationDetected = true;
		}
		relation = new ArrayList<>();
		type = WayType.UNKNOWN;
	}

	/**
	 * Used to store the member of a relation.
	 * @param atts The attributes of the member tag.
     */
	private void startMember(Attributes atts){
		id = Long.parseLong(atts.getValue("ref"));
		Way path = ways.get(id);
		if (path != null){
			relMap.put(path.startPointID(), path);
		}
	}

	/**
	 * Used to read tags.
	 * @param atts The attributes of the tag tag.
     */
	private void startTag(Attributes atts){
		switch (atts.getValue("k")) {
			// Attributes related to addresses. Needed for adding address nodes.
			case "addr:street":
				isAddressNode = true;
				street = atts.getValue("v").intern();
				break;
			case "addr:housenumber":
				housenumber = atts.getValue("v").intern();
				break;
			case "addr:postcode":
				if(atts.getValue("v").length() == 4) {
					postcode = Short.parseShort(atts.getValue("v"));
				}
				break;
			case "addr:city":
				city = atts.getValue("v").intern();
				break;
			// Attributes related to roads
			case "highway":
				String tmpValueV = atts.getValue("v");
				switch (tmpValueV) {
					case "cycleway":
						type = WayType.ROAD;
						roadType = RoadType.CYCLEWAY;
						break;
					case "steps":
						//Intentional fallthrough
					case "footway":
						//Intentional fallthrough
					case "pedestrian":
						//Intentional fallthrough
					case "path":
						type = WayType.WALKWAY;
						break;
					default:
						type = WayType.ROAD;
						roadType = roadType(tmpValueV);
						break;
				}
				break;
			case "name":
				name = atts.getValue("v").intern();
				break;
			// Road specific tags
			case "maxspeed":
				String speed = atts.getValue("v").replaceAll(" ", "");
				if (speed.endsWith("kph")) {
					maxSpeed = Short.parseShort(speed.substring(0, speed.length() - 3));
				} else if (speed.endsWith("mph")) {
					maxSpeed = (short) (Integer.parseInt(speed.substring(0, speed.length() - 3)) * 1.6);
				} else if (speed.endsWith("knots")) {
					maxSpeed = (short) (Integer.parseInt(speed.substring(0, speed.length() - 5)) * 1.8);
				} else {
					switch (speed) {
						case "signals":
							break;
						case "DK:urban":
							maxSpeed = 50;
							break;
						case "DK:rural":
							maxSpeed = 80;
							break;
						case "none":
							maxSpeed = 150;
							break;
						default:
							if (speed.contains(":") || atts.getValue("v").equals("*")) {
								break;
							} else {
								try {
									maxSpeed = Short.parseShort(atts.getValue("v"));
								} catch(NumberFormatException e){
									maxSpeed = -1;
								}
								break;
							}
					}
				}
				break;
			case "oneway":
				if (atts.getValue("v").equals("yes") || atts.getValue("v").equals("true")) {
					oneWay = 1;
				} else if (atts.getValue("v").equals("no") || atts.getValue("v").equals("false")) {
					oneWay = 0;
				} else if (Integer.parseInt(atts.getValue("v")) == -1) {
					oneWay = -1;
				} else {
					oneWay = 0;
				}
				break;
			// Building
			case "building":
				type = WayType.BUILDING;
				break;
			//Natural tags
			case "natural":
				switch (atts.getValue("v")) {
					case "coastline":
						type = WayType.COASTLINE;
						break;
					case "water":
						type = WayType.WATER;
						break;
					case "wood":
						type = WayType.FOREST;
						break;
					case "heath":
						type = WayType.GRASS;
						break;
					case "scrub":
						type = WayType.GRASS;
						break;
					case "grassland":
						type = WayType.GRASS;
						break;
					case "sand":
						type = WayType.SAND;
						break;
					case "beach":
						type = WayType.SAND;
						break;
				}
				break;
			case "amenity":
				amenityTag(atts);
				break;
			case "shop":
				shopTag(atts);
				break;
			case "leisure":
				leisureTag(atts);
				break;
			case "landuse":
				landuseTag(atts);
				break;
			case "aeroway":
				switch (atts.getValue("v")) {
					case "runway":
						type = WayType.AIRPORT;
						break;
					case "taxiway":
						type = WayType.AIRPORT;
						break;
				}
				break;
			case "waterway":
				type = WayType.WATERWAY;
				break;
			case "railway":
				switch (atts.getValue("v")) {
					case "light_rail":
						type = WayType.RAILWAY;
						break;
					case "rail":
						type = WayType.RAILWAY;
						break;
					case "tram":
						type = WayType.RAILWAY;
						break;
				}
				break;
			case "route":
				if(atts.getValue("v").equals("bicycle")){
					type = WayType.ROAD;
					roadType = RoadType.CYCLEWAY;
				}
				break;
			case "tourism":
				if(atts.getValue("v").equals("zoo") || atts.getValue("v").equals("theme_park")){
					type = WayType.PARKWALL;
				}
				break;
			case "place":
				switch(atts.getValue("v")){
					case "city":
						pType = POIType.CITY;
						break;
					case "village":
						pType = POIType.CITY;
						break;
					case "town":
						pType = POIType.CITY;
						break;
				}
				break;
			case "junction":
				if(atts.getValue("v").equals("roundabout")){
					roundabout = true;
				}
				break;
			case "access":
				switch (atts.getValue("v")){
					case "no":
					case "forestry":
					case "agricultural":
					case "restricted":
						noAccess = true;
						break;
					default:
						break;
				}
				break;
			case"bicycle":
				switch (atts.getValue("v")){
					case "forestry":
					case "agricultural":
					case "no":
					case "restricted":
						bikeRestriction = 0;
						break;
					case "permissive":
					case "allowed:":
					case "public":
					case "yes":
						bikeRestriction = 1;
						break;
					default:
						break;
				}
				break;
			case"motorcar":
				switch (atts.getValue("v")){
					case "agricultural":
					case "forestry":
					case "no":
					case "restricted":
						carRestriction = 0;
						break;
					case "permissive":
					case "public":
					case "yes":
						carRestriction = 1;
						break;
					default:
						break;
				}
				break;
			default:
				break;
		}
	}

	/**
	 * Used to determine the road type corresponding to the String.
	 * @param tmpValueV The String from the OSM-file.
	 * @return The corresponding road type.
     */
	private RoadType roadType(String tmpValueV) {
		switch(tmpValueV) {
			case "motorway":
				return RoadType.MOTORWAY;
			case "motorway_link":
				return RoadType.MOTORWAYLINK;
			case "trunk":
			case "trunk_link":
			case "primary":
			case "primary_link":
				return RoadType.PRIMARY;
			case "secondary":
			case "secondary_link":
				return RoadType.SECONDARY;
			case "road":
			case "tertiary":
			case "tertiary_link":
				return RoadType.TERTIARY;
			case "residential":
				return RoadType.RESIDENTIAL;
			case "living_street":
				return RoadType.LIVING;
			case "track":
				return RoadType.TRACK;
			case "bridleway":
			case "bus_guideway":
			case "construction":
			case "proposed":
			case "rest_area":
				return RoadType.IGNORE;
			case "service":
				return RoadType.SERVICE;
			case "unclassified":
				return RoadType.UNCLASSIFIED;
			default:
				return RoadType.UNKNOWN;
		}
	}
	
	/**
	 * Used to read shop tags.
	 * @param atts The attribute of the amenity tag.
	 */
	private void shopTag(Attributes atts) {
		switch (atts.getValue("v")) {
		case "convenience":
			pType = POIType.SHOP;
			break;
		case "supermarket":
			pType = POIType.SHOP;
			break;
		case "photo":
			pType = POIType.CAMERA;
			break;
		case "bicycle":
			pType = POIType.BICYCLE;
			break;
		case "car":
			pType = POIType.CAR;
			break;
		case "video_games":
			pType = POIType.STAR;
			break;
		
	}
	}

	/**
	 * Used to read the amenity tag.
	 * @param atts The attribute of the amenity tag.
     */
	private void amenityTag(Attributes atts) {
		switch (atts.getValue("v")) {
			case "parking":
				type = WayType.PARKING;
				break;
			case "grave_yard":
				type = WayType.GRASS;
				break;
			case "bar":
				pType = POIType.BAR;
				break;
			case "pub":
				pType = POIType.BAR;
				break;
			case "cafe":
				pType = POIType.BAR;
				break;
			case "fuel":
				pType = POIType.FUEL;
				break;
			case "food_court":
				pType = POIType.FOOD;
				break;
			case "fast_food":
				pType = POIType.FOOD;
				break;
			case "restaurant":
				pType = POIType.FOOD;
				break;
		}
	}

	/**
	 * Used to read leisure tag.
	 * @param atts The attributes of the leisure tag.
     */
	private void leisureTag(Attributes atts) {
		switch (atts.getValue("v")) {
			case "park":
				type = WayType.GRASS;
				break;
			case "garden":
				type = WayType.GRASS;
				break;
			case "pitch":
				type = WayType.GRASS;
				break;
			case "golf_course":
				type = WayType.GRASS;
				break;
			case "common":
				type = WayType.GRASS;
				break;
		}
	}

	/**
	 * Used to read the landuse tag.
	 * @param atts The attributes of the landuse tag.
     */
	private void landuseTag(Attributes atts) {
		switch (atts.getValue("v")) {
			case "allotments":
				type=WayType.RESIDENTIAL;
				break;
			case "basin":
				type = WayType.WATER;
				break;
			case "forest":
				type = WayType.FOREST;
				break;
			case "meadow":
				type = WayType.PLAIN;
				break;
			case "industrial":
				type = WayType.INDUSTRIAL;
				break;
			case "residential":
				type = WayType.RESIDENTIAL;
				break;
			case "farmland":
				type = WayType.FARMLAND;
				break;
			case "farm":
				type = WayType.FARMLAND;
				break;
			case "cemetery":
				//Maybe this should be drawn differently
				type = WayType.GRASS;
				break;
		}
	}

	public void endElement(String uri, String localName, String qName) {
		switch (qName) {
			case "node":
				endNode();
				break;
			case "way":
				endWay();
				break;
			case "relation":
				endRelation();
				break;
		}
	}

	/**
	 * Used to create addresses and POI's.
	 */
	private void endNode(){
		if(isAddressNode) {
			Point2D tempPoint = points.get(id);
			fac.address(new Address(city, housenumber, street, postcode, tempPoint));
			isAddressNode = false;
		}
		if(!pType.equals(POIType.UNKNOWN)){
			Point2D tempPoint = points.get(id);
			if (pType == POIType.CITY) {
				fac.cities(new POI(tempPoint, name, POIType.CITY));
			}
			model.POI(new POI(tempPoint, name, pType));
		}
	}

	/**
	 * Used to construct a way object of the correct type.
	 */
	private void endWay(){
		if(way.size() == 0){
			clearTmpVars();
			way = null;
			return;
		}
		ways.put(wayID, way);
		if(type == WayType.COASTLINE) {
			coasts.add(way);
			builder.coastline(wayToPointList(way));
			clearTmpVars();
		} else if (type == WayType.ROAD){
			if(roadType != RoadType.IGNORE) {
				if(roundabout){
					roads.add(new RoadWay(way, roadType, maxSpeed, name, (byte) 1, true, findRoadAccess()));
				} else {
					roads.add(new RoadWay(way, roadType, maxSpeed, name, oneWay, false, findRoadAccess()));
				}
				addPath(new PolygonApprox(wayToPointList(way)));
			}
			clearTmpVars();
		} else {
			addPath(new PolygonApprox(wayToPointList(way)));
		}
		way = null;
	}

	/**
	 * Used to find the road access.
	 * @return The road accessibility.
     */
	private RoadAccess findRoadAccess(){
		if(roadType == RoadType.MOTORWAY || roadType == RoadType.MOTORWAYLINK){
			bikeRestriction = 0;
		} else if(roadType == RoadType.CYCLEWAY){
			carRestriction = 0;
		}
		if(!noAccess && bikeRestriction != 0 || bikeRestriction == 1){
			if(!noAccess && carRestriction != 0 || carRestriction == 1){
				return RoadAccess.ALLALLOWED;
			} else {
				return RoadAccess.ONLYBIKE;
			}
		} else{
			if(!noAccess && carRestriction != 0 || carRestriction == 1){
				return RoadAccess.ONLYCARS;
			} else {
				return RoadAccess.NONEALLOWED;
			}
		}
	}

	/**
	 * Used to construct the relations.
	 */
	private void endRelation(){
		if(!notRelationTypes.contains(type)) {
			if(connectWays()) {
				boolean validRelation = true;
				for(List<?> w: relation){
					if(!w.get(0).equals(w.get(w.size()-1))){
						validRelation = false;
					}
				}
				if(validRelation){
					PolygonApprox path;
					if(relation.size() == 1){
						path = new PolygonApprox(relation.get(0));
					} else {
						path = new MultiPolygonApprox(relation);
					}
					addPath(path);
				}
			}
		}
		relMap = new HashMap<>();
		relation = null;
	}

	/**
	 * Used to connect the ways.
	 * @return Ignore on false.
     */
	private boolean connectWays(){
		Iterator<Long> it = relMap.keySet().iterator();
		List<Long> usedKeys = new ArrayList<>();
		while(it.hasNext()) {
			Long key = it.next();
			Way line = relMap.get(key);
			if(coasts.contains(line) && type == WayType.WATER){
				return false;
			}
			if (usedKeys.contains(key)) {
				continue;
			}
			Long current = line.endPointID();
			boolean connected = false;
			while (!connected) {
				if (key.equals(current)) {
					connected = true;
					continue;
				}
				if (relMap.containsKey(current) && !usedKeys.contains(current)) {
					//System.out.println(""+current.getX()/lonfactor+"y"+current.getY());
					Way cc = relMap.get(current);
					usedKeys.add(current);
					line.addAll(cc);
					current = line.endPointID();
				} else {
					break;
				}
			}
		}

		it = relMap.keySet().iterator();
		while(it.hasNext()){
			Long key = it.next();
			if(!usedKeys.contains(key)) {
				relation.add(wayToPointList(relMap.get(key)));
			}
		}
		return true;
	}

	/**
	 * Converts a way into a list of Point2D's.
	 * @param way The given way to convert.
	 * @return The converted way as a ArrayList.
     */
	private List<Point2D> wayToPointList(Way way){
		List<Point2D> list = new ArrayList<>();
		for(Long l : way){
			list.add(points.get(l));
		}
		return list;
	}

	/**
	 * Clears all the temporary variables.
	 */
	private void clearTmpVars() {
		maxSpeed = -1;
		oneWay = 0;
		noAccess = false;
		carRestriction = -1;
		bikeRestriction = -1;
		roundabout = false;
		name = "";
		roadType = RoadType.UNKNOWN;

		// Address attributes
		street = "";
		housenumber = "";
		city = "";
		postcode = 0;
		isAddressNode = false;
	}

	/**
	 * Adds a path to the factory.
	 * @param path The given path to be added.
     */
	private void addPath(PolygonApprox path){
		if(path == null) {
			clearTmpVars();
			return;
		}
		switch (type) {
			case ROAD:
				switch (roadType) {
					case MOTORWAY:
					case MOTORWAYLINK:
						fac.road_lvl1(path);
						break;
					case PRIMARY:
						fac.road_lvl2(path);
						break;
					case SECONDARY:
					case TERTIARY:
						fac.road_lvl3(path);
						break;
					case RESIDENTIAL:
					case UNCLASSIFIED:
						fac.road_lvl4(path);
						break;
					case LIVING:
					case TRACK:
					case SERVICE:
					case UNKNOWN:
						fac.road_lvl5(path);
						break;
					case CYCLEWAY:
						fac.cycleway(path);
						break;
					default:
						break;
				}
				break;
			case WALKWAY:
				fac.walkway(path);
				break;
			case BUILDING:
				fac.building(path);
				break;
			case FOREST:
				fac.forest(path);
				break;
			case PLAIN:
				fac.plain(path);
				break;
			case GRASS:
				fac.grass(path);
				break;
			case WATER:
				fac.water(path);
				break;
			case WATERWAY:
				fac.waterway(path);
				break;
			case INDUSTRIAL:
				fac.industrial(path);
				break;
			case RESIDENTIAL:
				fac.residential(path);
				break;
			case PARKING:
				fac.parking(path);
				break;
			case PARKWALL:
				model.parkWall(path);
				break;
			case AIRPORT:
				model.airport(path);
				break;
			case RAILWAY:
				fac.railway(path);
				break;
			case FARMLAND:
				fac.farmland(path);
				break;
			case SAND:
				fac.sand(path);
				break;
			default:
				break;
		}
		clearTmpVars();
	}
	@Override
	public void endDocument() {
		load.loadingText("Creating Navigation Graph...");
		createRoadNetwork();
		load.loadingText("Creating DataTrees...");
		createDataTrees();
		load.dispose();
	}

	/**
	 * Creates the graph necessary for the pathfinder.
	 */
	private void createRoadNetwork(){
		findSplittingPoints();
		List<Vertex> vertices = splitOnVertices();
		model.pathFinder(new PathFinder(vertices));
	}

	/**
	 * Finds the vertices needed for the navigation graph.
	 */
	private void findSplittingPoints(){
		/*
		Iterates over all point-ID of every road.
		If a point is found in more than one road it is an intersection of some sort
		and is saved as a "splitting point" for the road
		 */
		Map<Long, List<RoadWay>> roadPointMap = new HashMap<>();
		for(RoadWay rw : roads){
			for(Long ID : rw){
				if(!roadPointMap.containsKey(ID)){
					List<RoadWay> roadways = new ArrayList<>();
					roadways.add(rw);
					roadPointMap.put(ID, roadways);
				} else {
					List<RoadWay> roadways = roadPointMap.get(ID);
					if(roadways.size() == 1){
						roadways.get(0).addSplitPoint(ID);
						roadways.add(rw);
					}
					rw.addSplitPoint(ID);
				}
			}
		}
	}

	/**
	 * Splits up roads into smaller parts.
	 * @return A list of the vertices after splitting roads.
     */
	private List<Vertex> splitOnVertices(){
		/*
		Splits up every road on the found splitting points
		Roads only know the vertices as an index, and can access them later through the Pathfinder
		 */
		HashMap<Long, Integer> indexMap = new HashMap<>();
		ArrayList<Vertex> vertices = new ArrayList<>();
		vertices.add(new Vertex(0)); // DO NOT REMOVE! HELL WILL BREAK LOOSE!
		for(RoadWay original : roads) {
			RoadWay[] newWays = original.split();
			if(newWays == null) {continue; }
			for(RoadWay split : newWays) {
				//The serialize and duplicate-vertex-problem
				int fromIndex, toIndex;				
				if(indexMap.containsKey(split.startPointID())) {
					fromIndex = indexMap.get(split.startPointID()); 
				} else {
					fromIndex = vertices.size();
					indexMap.put(split.startPointID(), fromIndex);
					vertices.add(new Vertex(fromIndex));
				}
				
				if(indexMap.containsKey(split.endPointID())) {
					toIndex = indexMap.get(split.endPointID()); 
				} else {
					toIndex = vertices.size();
					indexMap.put(split.endPointID(), toIndex);
					vertices.add(new Vertex(toIndex));
				}
				
				//The one-way problem
				Road currentRoad;
				if(split.isRoundabout()){
					currentRoad = new Roundabout(wayToPointList(split), split.access(), split.name(), split.speed(), fromIndex, toIndex);
					vertices.get(fromIndex).addRoad(currentRoad);
				} else if(split.oneWay() == 0) {
					currentRoad = new Road(wayToPointList(split), split.access(), split.name(), split.speed(), fromIndex, toIndex);
					vertices.get(fromIndex).addRoad(currentRoad);
					vertices.get(toIndex).addRoad(currentRoad);
				} else if (split.oneWay() > 0) {
					currentRoad = new Road(wayToPointList(split), split.access(), split.name(), split.speed(), fromIndex, toIndex);
					vertices.get(fromIndex).addRoad(currentRoad);
				} else {
					currentRoad = new Road(wayToPointList(split), split.access(), split.name(), split.speed(), fromIndex, toIndex);
					vertices.get(toIndex).addRoad(currentRoad);
				}
				//Finally done! Add it!
				fac.navigationRoad(currentRoad);
			}
		}
		return vertices;
	}

	/**
	 * Converts parsed data into data trees.
	 */
	private void createDataTrees(){
		builder.sortCoast();
		model.address(fac.address());
		model.cities(fac.cities());
		model.dataCoast(new DataTree(builder.coast(), false));
		model.dataBuilding(fac.buildings());
		model.dataCycle(fac.cycleway());
		model.dataFarm(fac.farmland());
		model.dataForest(fac.forest());
		model.dataGrass(fac.grass());
		model.dataIndustrial(fac.industrial());
		model.dataParking(fac.parking());
		model.dataPlain(fac.plain());
		model.dataRailway(fac.railway());
		model.dataResidential(fac.residential());
		model.dataNavigation(fac.navigationRoad());
		model.dataRoad_lvl1(fac.roads_lvl1());
		model.dataRoad_lvl2(fac.roads_lvl2());
		model.dataRoad_lvl3(fac.roads_lvl3());
		model.dataRoad_lvl4(fac.roads_lvl4());
		model.dataRoad_lvl5(fac.roads_lvl5());
		model.dataSand(fac.sand());
		model.dataWalk(fac.walkway());
		model.dataWater(fac.water());
		model.dataWaterway(fac.waterway());
	}

	/**
	 * Node class by Troels Bjerre Lund.
	 */
	private class Node extends Point2D.Float {
		private static final long serialVersionUID = 16052016L;
		Node next;
		long key;

		public Node(long _key, float x, float y, Node _next) {
			super(x, y);
			key = _key;
			next = _next;
		}
	}

	/**
	 * IdMap class by Troels Bjerre Lund.
	 */
	private class IdMap {
		int MASK;
		public Node[] tab;

		public IdMap(int capacity) {
			tab = new Node[1 << capacity];
			MASK = tab.length - 1;
		}

		public void put(long key, float x, float y) {
			int h = Long.hashCode(key) & MASK;
			tab[h] = new Node(key, x, y, tab[h]);
		}

		public Point2D get(long key) {
			for (Node n = tab[Long.hashCode(key) & MASK] ; n != null ; n = n.next) {
				if (n.key == key) return n;
			}
			return null;
		}
	}

}