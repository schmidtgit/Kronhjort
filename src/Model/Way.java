package Model;
import java.util.ArrayList;

/**
 * OSMWay by Troels Bjerre Lund.
 */
public class Way extends ArrayList<Long> {
	private static final long serialVersionUID = 16052016L;

    /**
     * Returns the ID of the first point.
     * @return -1L if way is empty.
     */
	public long startPointID(){
        if(size() == 0){
            return -1L;
        } else {
            return get(0);
        }
    }

    /**
     * Returns the ID of the last point.
     * @return -1L if way is empty.
     */
    public long endPointID(){
        if(size() == 0){
            return -1L;
        } else {
            return get(size() - 1);
        }
    }
}
