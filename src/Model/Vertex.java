package Model;
import enums.RoadAccess;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Vertex objects represents every intersection between roads and functions as vertices in our navigation graph.
 */
public class Vertex implements Serializable, Comparable<Vertex> {
	private static final long serialVersionUID = 16052016L;
	private float minDist;
	protected float heuristic;
    private ArrayList<Road> edges;
    private Road edgeTo;
    private int index;
    
    /**
     * Vertex takes an index that must be unique for each object and represent it's index in PathFinders array of vertices.
     * @param index
     */
    public Vertex(int index) {
    	this.index = index;
        edges = new ArrayList<>();
        minDist = Float.POSITIVE_INFINITY;
        heuristic = Float.POSITIVE_INFINITY;
        edgeTo = null;
    }
    
    /**
     * Returns the point in world-space coordinates where the vertex is located.
     * @return Point2D 
     */
    public Point2D getPosition() { 
    	if(edges.size() > 0) {
    		return edges.get(0).vertexPosition(this);
    	} else {
    		return null;
    	}
    }

    /**
     * Adds a road to the vertex. Only do this if the road is physically connected to the other roads in
     * this vertex, and you are allowed to drive on that road coming from this vertex.
     * @param road
     */
    public void addRoad(Road road) { edges.add(road); }
    
    /**
     * Used to set current minimum distance from the navigation start point. Used for Dijkstra.
     * @param f
     */
    public void minDist(float f) { minDist = f; }
    
    /**
     * Used to return the current minimum distance from the navigation start point. Used for Dijkstra.
     * @return
     */
    public float minDist() { return minDist; }

    /**
     * Sets the heuristic.
     */
    public void heuristic(double d) {
    	heuristic = (float) d;
    }

    /**
     * Used for Dijkstra.
     * @param road
     */
    public void edgeTo(Road road) { edgeTo = road; }
    
    /**
     * Used for Dijkstra.
     * @return Road
     */
    public Road edgeTo() { return edgeTo; }    
    
    /**
     * Returns only the accessible roads.
     * @param car True if by car, false if by bike.
     * @return Road[] of all allowed roads to take.
     */
    public Road[] edges(boolean car) {
        ArrayList<Road> allowed = new ArrayList<>();;
        if(car){
            for(Road r : edges){
                if(r.access() == RoadAccess.ALLALLOWED || r.access() == RoadAccess.ONLYCARS){
                    allowed.add(r);
                }
            }
        } else {
            for(Road r : edges){
                if(r.access() == RoadAccess.ALLALLOWED || r.access() == RoadAccess.ONLYBIKE){
                    allowed.add(r);
                }
            }
        }
        return allowed.toArray(new Road[allowed.size()]);
    }
    
    /**
     * Clears all fields, makes vertex ready for next navigation search.
     */
    public void reset() {
    	minDist = Float.POSITIVE_INFINITY;
    	edgeTo = null;
    	heuristic = Float.POSITIVE_INFINITY;
    }
    
    /**
     * Returns the index corresponding to the one in PathFinder.
     * @return
     */
    public int index() { return index; }

	@Override
	public int compareTo(Vertex that) {
		return Float.compare(this.minDist + this.heuristic, that.minDist() + that.heuristic);
	}
}
