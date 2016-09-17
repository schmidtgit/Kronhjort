package Model;
import enums.RoadAccess;
import enums.RoadType;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * An extension of Way which is used for splitting the roads at intersections
 * during parsing by the OSM Handler.
 */
public class RoadWay extends Way {
	private static final long serialVersionUID = 16052016L;
	private RoadType type;
    RoadAccess access;
    private boolean roundabout;
    private short speed;
    private String name;
    private byte oneWay;
    private ArrayList<Integer> splitList;

    /**
     * Constructor for roadways
     * @param way The way representing the road (ArrayList of point ID's)
     * @param type The type of road
     * @param speed The speed limit in km/h
     * @param name Name of road
     * @param oneWay Is the road one way? -1: oneway against drawing direction. 0: not oneway. 1: oneway with
     *               drawing direction.
     * @param roundabout Is this road part of a roundabout?
     * @param access Enum representing the accessibility of this road.
     */
    public RoadWay(Way way, RoadType type, short speed, String name, byte oneWay, boolean roundabout, RoadAccess access) {
        this.type = type;
        this.name = name;
        this.oneWay = oneWay;
        this.roundabout = roundabout;
        if(speed < 1) {
            this.speed = defaultSpeed();
        } else {
            this.speed = speed;
        }
        this.access = access;

        splitList = new ArrayList<>();

        for(long l : way) {
            add(l);
        }
    }

    /**
     * Adding the osm-ID of a point to the array of points to split this roadway.
     * @param id osm-ID of split point.
     */
    public void addSplitPoint(Long id) {
        //Do not add split points twice. No split if splitPoint is start- or endpoint.
        try {
            if (!(id.equals(startPointID()) || id.equals(endPointID()) || splitList.contains(indexOf(id)))) {
                if (id >= 0) {
                    splitList.add(indexOf(id));
                }
            }
        } catch (NullPointerException e){
        }
    }

    /**
     * Splits this RoadWay into an array of new RoadWay's. Used to create navigation graph.
     * @return List of RoadWay's to be made into actual Road's later
     */
    public RoadWay[] split() {
        if(size() < 2){
            return null;
        }
        RoadWay[] returnArray = new RoadWay[splitList.size() + 1];
        if(splitList.size() < 1) {
            returnArray[0] = this;
        } else {
            int[] splits = new int[splitList.size()];
            for(int i = 0; i < splitList.size(); i++) {
                splits[i] = splitList.get(i);
            }
            Arrays.sort(splits);
            int previousIndex = 0;
            int returnArrayIndex = 0;
            for (int splitPoint : splits) {
                //int index = indexOf(splitPoint);
                Way tmpWay = new Way();
                for (int i = previousIndex; i <= splitPoint; i++) {
                    tmpWay.add(get(i));
                }

                returnArray[returnArrayIndex++] = (new RoadWay(tmpWay, type, speed, name, oneWay, roundabout, access));
                previousIndex = splitPoint;
            }
            Way tmpWay = new Way();
            for(int i = previousIndex; i < size(); i++) {
                tmpWay.add(get(i));
            }
            returnArray[returnArrayIndex++] = (new RoadWay(tmpWay, type, speed, name, oneWay, roundabout, access));
        }
        return returnArray;
    }

    /**
     * If a RoadWay is constructed without a speed this method sets a speed based on the RoadType.
     * @return The speed in km/t.
     */
    private short defaultSpeed() {
        short returnSpeed = 13; // 13 to be able to see if we ever fall all the way through
        switch(type) { //All fallthroughs are intentional
            case MOTORWAY:
                returnSpeed = 130;
                break;
            case MOTORWAYLINK:
                returnSpeed = 90;
                break;
            case PRIMARY: // Might be a "landevej"?
            case SECONDARY: // Might be another "landevej"?
                returnSpeed = 80;
                break;
            case TERTIARY: // Might be a main road through a city.
            case RESIDENTIAL:
            case SERVICE:
            case UNCLASSIFIED:
            case UNKNOWN:
                returnSpeed = 50;
                break;
            case CYCLEWAY:
                returnSpeed = 20;
                break;
            case LIVING:
            case TRACK:
            case IGNORE:
                returnSpeed = 15;
                break;
            default:
                break;
        }
        return returnSpeed;
    }

    /**
     * Setter for speed.
     * @param speed The speed in km/t.
     */
    public void speed(short speed) {
        this.speed = speed;
    }

    /**
     * Returns the road type.
     * @return The type of this road.
     */
    public RoadType type() {return type;}

    /**
     * Returns the accessibility of this road.
     * @return The access for this road.
     */
    public RoadAccess access() {return access;}

    /**
     * Returns the speed limit.
     * @return Upper speed limit in km/t.
     */
    public short speed() {return speed;}

    /**
     * Returns the RoadWay's name.
     * @return OSM name for road.
     */
    public String name() {return name;}

    /**
     * Is this road oneway?
     * @return -1: oneway against drawing direction, 0: not oneway, 1: oneway in drawing direction.
     */
    public byte oneWay() {return oneWay;}

    /**
     * Is this part of a roundabout.
     * @return True: is a roundabout. False: is NOT a roundabout.
     */
    public boolean isRoundabout() {return roundabout;}
}