package Model;
import java.awt.geom.Point2D;
import enums.POIType;

/**
 * Extends POI to allow addition of description.
 */
public class UserPOI extends POI {
	private static final long serialVersionUID = 16052016L;
	String desc;

	/**
	 * Initializes the UserPOI.
	 * @param location the location of the UserPOI.
	 * @param name the name of the UserPOI.
	 * @param description the description of the UserPOI.
     * @param type the type of the UserPOI, which is based on the enum POIType.
     */
	public UserPOI(Point2D location, String name, String description, POIType type) {
		super(location, name, type);
		desc = description;
	}

	/**
	 * Returns the description of the UserPOI.
	 * @return the description of the UserPOI.
     */
	public String description() {return desc;}

	/**
	 * This toString method is used for the POIlist, to show the UserPOI on the
	 * POIlist in the sidePanel.
	 * @return a concatenation of the name and description of the UserPOI, divided
	 * by a vertical bar seperator.
     */
	public String toString(){ return name + " | " + desc;}
}
