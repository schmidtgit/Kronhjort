package Model;
import enums.POIType;
import java.awt.geom.Point2D;
import java.io.Serializable;

public class POI extends Point2D.Float implements Serializable, Comparable<POI> {
	private static final long serialVersionUID = 16052016L;
	String name;
	POIType type;

	/**
	 * Initializes a point of interest (POI).
	 * @param location the location of the POI.
	 * @param name the name of the POI.
	 * @param type the type of the POI.
     */
	public POI(Point2D location, String name, POIType type) {
		super((float) location.getX(), (float) location.getY());
		this.name = name;
		this.type = type;
	}

	/**
	 * Returns the POI type.
	 * @return
     */
	public POIType type(){ return type;}

	/**
	 * Returns the name.
	 * @return
     */
	public String name() { return name;}

	@Override
	public String toString(){ return name;}

	@Override
	public int compareTo(POI that){
			return name.compareTo(that.name());
	}
}