package Model;
import enums.RoadAccess;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * Used to differentiate between normal roads and roundabouts.
 */
public class Roundabout extends Road {
	private static final long serialVersionUID = 16052016L;

    /**
     * Sends all parameters to the Road constructor.
     * @param path
     * @param roadAccess
     * @param name
     * @param speed
     * @param startIndex
     * @param endIndex
     */
	public Roundabout(List<Point2D> path, RoadAccess roadAccess, String name, short speed, int startIndex, int endIndex) {
        super(path, roadAccess, name, speed, startIndex, endIndex);
    }

    /**
     * Checks if this is a roundabout
     * @return true
     */
    public boolean isRoundabout() {return true;}
}
