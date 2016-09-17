package Model;

import Controller.Main;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The address class is responsible for storing address information and tie it to a world location.
 */
public class Address extends Point2D.Float implements Comparable<Address>, Serializable {
	private static final long serialVersionUID = 16052016L;
	private final String city, housenumber, street;
	private short postcode;

	/**
	 * The address class is responsible for storing address information and tie it to a world location.
	 * @param city The city in which the address is located.
	 * @param housenumber The house number of the address, including letters.
	 * @param street The street name of the address. It's expected to match a road name.
	 * @param postcode Expected to be a legal Danish postcode. Does not handle DR-Byen and other special cases starting with 0.
	 * @param lon The longitude of the location.
	 * @param lat The latitude of the location.
	 */
	public Address(String city, String housenumber, String street, short postcode, float lon, float lat) {
		this.city = city; this.housenumber = housenumber; this.street = street; this.postcode = postcode; setLocation(lon,lat);
	}
	
	/**
	 * The address class is responsible for storing address information and tie it to a world location.
	 * @param city The city in which the address is located.
	 * @param housenumber The house number of the address, including letters.
	 * @param street The street name of the address. It's expected to match a road name.
	 * @param postcode Expected to be a legal Danish postcode. Does not handle DR-Byen and other special cases starting with 0.
	 * @param p Expected to contain the longitude and latitude of the location.
	 */
	public Address(String city, String housenumber, String street, short postcode, Point2D p) {
		this.city = city; this.housenumber = housenumber; this.street = street; this.postcode = postcode; setLocation(p.getX(),p.getY());
	}
	
	/**
	 * Returns the city name.
	 * @return The name of the city the address is located in. Might return null.
	 */
	public String city() {return city;}
	
	/**
	 * Returns the house number.
	 * @return The house number can contain letters or return null.
	 */
	public String housenumber() {return housenumber;}
	
	/**
	 * Returns the street name.
	 * @return The street name is not guaranteed to match any roads in the osm.
	 */
	public String street() {return street;}
	
	/**
	 * Returns the post code.
	 * @return The post code might not be a legal DK post code.
	 */
	public short postcode() {return postcode;}
	
	@Override
	public String toString() {return (street + " " + housenumber + ", " + postcode + " " + city);}
	
	/**
	 * Returns the closest road segment with the same name as this street address.
	 * If no roads within range match the road name, the closest segment will be returned no matter the name.
	 * @return A road close to the address. Might return null.
	 */
	public Road road() {
		//Get roads closest to this address.
		List<PolygonApprox[]> list = Main.model().dataNavigation(new Rectangle2D.Float((float) getX() - 0.001f, (float) getY() - 0.001f, 0.002f, 0.002f));
		List<Road> match = new ArrayList<>();
		
		//Find all road segments with the same street name.
		for(PolygonApprox[] array : list) {
			for(PolygonApprox pa : array) {
				Road r = (Road) pa;
				if(r.name().equalsIgnoreCase(street)) {
					match.add(r);
				}
			}
		}
		
		//In case nothing matches, look at everything
		if(match.size() == 0) {
			for(PolygonApprox[] array : list) {
				for(PolygonApprox pa : array) {
					match.add((Road) pa);
				}
			}
		}
		
		//Find the closest road segment
		Road closest = null;
		float minDist = java.lang.Float.POSITIVE_INFINITY;
		for(Road r : match) {
			if(minDist > distanceCalc(r)) {
				minDist = distanceCalc(r);
				closest = r;
			}
		}
		return closest;
	}

	/**
	 * Compares addresses according to street, name, postcode, and housenumber.
	 * @param a The address to compare with.
	 * @return
     */
	public int compareTo(Address a) {
		if(street.equals(a.street())) {
			if(postcode == a.postcode) {
				if(house() != a.house()) {
					return house() - a.house();
				} else {
					return housenumber.compareTo(a.housenumber());
				}
			}
			return Short.compare(postcode, a.postcode());
		}
		return street.compareTo(a.street());
	}

	/**
	 * Returns the value of all digits placed before the first letter in the house number.
	 * @return Returns 0 if the house number is null or does not contain any digits before a letter.
	 */
	protected int house() {
		if(housenumber == null || housenumber.length() == 0 ) {return 0;}
		char cArray[] = housenumber.toCharArray();
		int i = 0;
		for(char c : cArray) {
			if(!Character.isDigit(c)) {
				break;
			}
			i++;
		}
		if(i == 0) {return 0;}
		return Integer.parseInt(housenumber.substring(0,i));
	}
	
	/**
	 * Calculates the shortest distance between the given road and this address.
	 * @param r The given road is expected to not be null.
	 * @return The distance in world-coordinates to the closest point on the road.
	 */
	private float distanceCalc(Road r) {
		PathIterator iterator = r.getPathIterator(null);
		float minDist = java.lang.Float.MAX_VALUE; float currentDist;
		float[] coords = new float[2]; float[] prevCoords = new float[2];
		iterator.currentSegment(prevCoords); iterator.next();

		while(!iterator.isDone()) {
			iterator.currentSegment(coords);
			currentDist = (float) Math.abs(new Line2D.Float(prevCoords[0],prevCoords[1],coords[0],coords[1]).ptSegDist(this));
			if(java.lang.Float.compare(currentDist, minDist) < 0) {minDist = currentDist;}
			prevCoords[0] = coords[0]; prevCoords[1] = coords[1];
			iterator.next();
		}
		return minDist;
	}
}