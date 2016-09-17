package Model;
import enums.RoadAccess;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Road objects are used as edges in the Dijkstra algorithm and to draw the roads on the map.
 */
public class Road extends PolygonApprox {
	private static final long serialVersionUID = 16052016L;
	final protected RoadAccess roadAccess;
	final protected short speed;
	final protected String name;
	final protected int startIndex, endIndex;
	final protected int length;
	
	@Deprecated
	public Road(List<Point2D> path, RoadAccess roadAccess, String name, short speed, byte oneway, int startIndex, int endIndex) {
		this(path,roadAccess,name,speed,startIndex,endIndex);
	}
	
	@Deprecated
	public Vertex closestVertex() {
		if(startIndex == 0) {return PathFinder.vertex(endIndex);}
		return PathFinder.vertex(startIndex);
	}
	
	/**
	 * All variables are stored as final.
	 * @param path The path the road follows.
	 * @param roadAccess Who may drive on this road.
	 * @param name The street name of this road.
	 * @param speed	Maximum speed allowed.
	 * @param startIndex Index of the vertex.
	 * @param endIndex	Index of the vertex.
	 */
	public Road(List<Point2D> path, RoadAccess roadAccess, String name, short speed, int startIndex, int endIndex) {
		super(path);
		this.name = name;
		this.roadAccess = roadAccess;
		this.speed = speed;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.length = calculateLength(path);
	}

	/**
	 * Checks if this is a roundabout.
	 * @return Always false.
     */
	public boolean isRoundabout(){ return false;}

	/**
	 * Returns the RoadAccess level.
	 * @return
	 */
	public RoadAccess access() {return roadAccess;}
	
	/**
	 * Returns the length of the road in meters.
	 * @return
	 */
	public int length() {return length;}

	/**
	 * Returns the name of the road
	 * @return
     */
	public String name() {return name;}

	/**
	 * Returns the maximums allowed speed in km/h.
	 * @return
	 */
	public int speed() {return speed;}
	
	/**
	 * Takes a vertex, start or end, and returns the vertex in the other end of the road.
	 * @param v The opposite vertex.
	 * @return The other Vertex of the road than the Vertex object parameter.
	 */
	public Vertex otherVertex(Vertex v) {
		if(v.index() == startIndex) {
			return PathFinder.vertex(endIndex);
		} else if (v.index() == endIndex) {
			return PathFinder.vertex(startIndex);
		}
		return null;
	}
	
	/**
	 * Returns the closest vertex to the point given.
	 * @param point The given point which it is calculated from.
	 * @return
	 */
	public Vertex closestVertex(Point2D point) {
		if(point.distance(this.startPoint()) > point.distance(this.endPoint())) {
			return PathFinder.vertex(endIndex);	
		} else { return PathFinder.vertex(startIndex);}
	}
	
	/**
	 * Returns the vertex that this road and another road has in common.
	 * @param r The Road which the Vertex object is shared with.
	 * @return
	 */
	public Vertex sharedVertex(Road r) {
		if(startIndex == r.startIndex() || startIndex == r.endIndex()) {
			return PathFinder.vertex(startIndex);
		} else if (endIndex == r.startIndex() || endIndex == r.endIndex()) {
			return PathFinder.vertex(endIndex);
		}
		return null;
	}
	
	/**
	 * Returns the next path point after the given vertex' point
	 * @param v The second Point2D from the Vertex object.
	 * @return
	 */
	public Point2D secondPosition(Vertex v) {
		if(v.index() == startIndex) {
			return secondPoint();
		} else if (v.index() == endIndex) {
			return secondLastPoint();
		}
		return null;
	}
	
	/**
	 * Returns the position of the given vertex
	 * @param v The given Vertex object, whose position is to be calculated.
	 * @return
	 */
	public Point2D vertexPosition(Vertex v){
		if(v.index() == startIndex) {
			return startPoint();
		} else if (v.index() == endIndex) {
			return endPoint();
		}
		return null;
	}
	
	/**
	 * Used to find out how many seconds it would take to cover the
	 * length of the road at maximum speed.
	 * @return
	 */
	public float travelTime() {
		float mPerMin = speed * 16.66f;
		return length/mPerMin;
	}
	
	/**
	 * Calculates the length of the given road
	 * @param road The list of Point2D used to calculate length of road.
	 * @return Returns the result in meters.
	 */
	private int calculateLength(List<Point2D> road){
		float sum = .0f;
		Point2D prevPoint = road.get(0);
		for(int i = 1; i<road.size(); i++){
			Point2D currentPoint = road.get(i);
			sum += Math.hypot(Math.abs(currentPoint.getX() - prevPoint.getX()), Math.abs(currentPoint.getY() - prevPoint.getY()));
			prevPoint = currentPoint;
		}
		sum *= Model.METER_CONVERSION;
		return (int) sum;
	}
	
	/**
	 * Returns a road object from the p-point to the nearest point on this road,
	 * and a road from that point to the Vertex object nearest the goal point.
	 * @param p Where the walkway should go to.
	 * @param goal A position near the vertex the path is going.
	 * @return A shorter version of the current road.
	 */
	public Road[] walkWay(Point2D p, Point2D goal) {
		List<Point2D> pathPoints = toList();
		Line2D road = null;
		double minDist = Double.POSITIVE_INFINITY;
		for(int i = 1; i < pathPoints.size(); i++) {
			if(new Line2D.Float(pathPoints.get(i-1), pathPoints.get(i)).ptSegDist(p) < minDist) {
				road = new Line2D.Float(pathPoints.get(i-1), pathPoints.get(i));
				minDist = road.ptSegDist(p);
			}
		}
		
		double deltaX = road.getX1() - road.getX2();
		double deltaY = road.getY1() - road.getY2();
		double length = Math.hypot(Math.abs(deltaX), Math.abs(deltaY));
		double normX = deltaX / length;
		double normY = deltaY / length;
		double c = road.ptLineDist(p);
		ArrayList<Point2D> path = new ArrayList<>();
		path.add(p);
		Point2D p1 = new Point2D.Double(p.getX()+(-normY*c),p.getY()+(normX*c));
		Point2D p2 = new Point2D.Double(p.getX()-(-normY*c),p.getY()-(normX*c));
		Road[] returnR = new Road[2];
		if(road.ptLineDist(p1) < road.ptLineDist(p2)) {
			path.add(p1);
			returnR[1] = halfRoad(p1, goal);
		} else { 
			path.add(p2);
			returnR[1] = halfRoad(p2, goal);
		}
		returnR[0] = new Road(path, RoadAccess.ONLYBIKE, "[WALKWAY]", (short) 15, 0, 0);
		return returnR;
	}
	
	/**
	 * Creates a new road based of the current road, starting at the point on the road
	 * closest to p and ending at the road-end nearest the goal point.
	 * @param p Where the walkway starts.
	 * @param goal A position near the vertex the path is going.
	 * @return A new smaller road, with the same vertices with as the old.
	 */
	private Road halfRoad(Point2D p, Point2D goal) {
		List<Point2D> list = this.toList();
		Point2D closestPoint = null;
		double minDist = Float.POSITIVE_INFINITY;
		for(Point2D p2 : list) {
			if(minDist > p.distance(p2)) {
				minDist = p.distance(p2);
				closestPoint = p2;
			}
		}
		
		int sIndex = 0, eIndex = 0;
		
		int indexOf = list.indexOf(closestPoint);
		List<Point2D> result = new ArrayList<Point2D>();
		if(goal.distance(this.startPoint()) < goal.distance(this.endPoint())){
			for(int i = 0; i < indexOf; i++) {
				result.add(list.get(i));
			}
			if(result.size() == 0) {result.add(list.get(0));}
			sIndex = this.startIndex;
			eIndex = this.endIndex;
		} else {
			for(int i = list.size()-1; i > indexOf; i--) {
				result.add(list.get(i));
			}
			if(result.size() == 0) {result.add(list.get(list.size()-1));}	
			eIndex = this.startIndex;
			sIndex = this.endIndex;
		}
		if(result.get(result.size()-1).distance(list.get(indexOf)) < result.get(result.size()-1).distance(p)) {
			result.add(list.get(indexOf));
		}
		result.add(p);		
		return new Road(result, this.roadAccess, this.name(), (short)this.speed(), sIndex, eIndex);
	}
	
	/**
	 * Returns the index of the vertex connected to the start of this road (drawing order).
	 * @return If 0, don't trust the index.
	 */
	protected int startIndex() { return startIndex; }
	
	/**
	 * Returns the index of the vertex connected to the start of this road (drawing order).
	 * @return If 0, don't trust the index.
	 */
	protected int endIndex() { return endIndex; }	
}
