package enums;

/**
 * Used to describe different kinds of map element.
 */
public enum ConfigType {	
	//Roads
	MOTORWAY ("Motorway"),
	BIGROADS ("Big Roads"),
	SMALLROADS ("Small Roads"),
	AIRWAY ("Airport Runway"),
	RAILWAY ("Railway"),
	CYCLE ("Cycleway"),
	WALKWAY ("Walkway"),
	ROUTE ("Navigation Route"),

	//Nature
	WATER ("Water"),
	FARMLAND ("Farmland"),
	BACKGROUND ("Background"),
	FOREST ("Forest"),
	PLAIN ("Plain"),
	GRASS ("Grass"),
	SAND ("Sand"),

	//Urban
	BUILDING ("Building"),
	INDUSTRIAL ("Industial zone"),
	RESIDENTIAL ("Residential zone"),
	PARKING ("Parking lot");

	private final String name;
	private ConfigType(String s) { name = s; }
	 
	@Override
	public String toString() { return name; }
}
