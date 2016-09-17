package Model;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import Controller.Main;

/**
 * This object is responsible for all navigation in this program.
 */
public class PathFinder implements Serializable {
	private static final long serialVersionUID = 16052016L;
	private Vertex[] vertices;
	private HashSet<Vertex> marked;
	private HashSet<Vertex> visited;
	private Point2D start, end;
	private boolean dragging, flipPath;
	private PriorityQueue<Vertex> queue;
	
	/**
	 * The pathfinder takes a list of all Vertex objects in the whole program.
	 * @param vertices A Vertex object for each road intersection is expected.
	 */
	public PathFinder(List<Vertex> vertices) {
		this.vertices = new Vertex[vertices.size()];
		vertices.toArray(this.vertices);
		marked = new HashSet<Vertex>();
		visited = new HashSet<Vertex>();
	}
	
	@Deprecated
	public Road[] shortestPath(Address from, Address to, boolean car, boolean fastest) throws NoPathFoundException {
		return shortestPath(from, to); 
	}
	
	@Deprecated
	public Road[] shortestPath(Road from, Road to) throws NoPathFoundException {
		return path(from.closestVertex(), to.closestVertex(), false, true);
	}
	
	@Deprecated
	public Road[] shortestPath(Address from, Address to) throws NoPathFoundException {
		return path(from.road().closestVertex(), to.road().closestVertex(), false, true);
	}
	
	/**
	 * Returns a route between two Point2D including the "walkway" to the address/POI.
	 * Walkways are temporary roads used to depict the complete route.
	 * @param to The Point2D you want to navigate to.
	 * @param from The Point2D you want to navigate from
	 * @param car If true, returns a car-friendly route. If false, returns a bike-friendly.
	 * @param fastest If true, return fastest path. If false, returns shortest. The fastest bike route does not make sense...
	 * @return The path.
	 * @throws NoPathFoundException Throws an exception if no road was found!
	 */
	public Road[] path(Point2D from, Point2D to, boolean car, boolean fastest) throws NoPathFoundException {
		start = from; end = to;
		Vertex f, t;
		
		if(start instanceof Address) {
			f = ((Address) start).road().closestVertex(start);
		} else {
			Road tmp = Main.model().closestRoad(start);
			if(tmp != null) {
				f = tmp.closestVertex(start);
			} else { throw new NoPathFoundException(); }
		}
		
		if(end instanceof Address) {
			t = ((Address) end).road().closestVertex(end);
		} else {
			Road tmp = Main.model().closestRoad(end);
			if(tmp != null) {
				t = tmp.closestVertex(end);
			} else { throw new NoPathFoundException(); }
		}
		
		return path(f,t,car,fastest);
	}
	
	/**
	 * Enable or disable dragging. Turns graph-cleaning off and on. Use with caution!
	 * @param dragging If true, turns of graph-cleaning.
	 * @param flipPath Flips the path if true.
	 */
	public void drag(boolean dragging, boolean flipPath) {
		if(dragging) {
			this.dragging = true;
			this.flipPath = flipPath;
		} else {
			this.dragging = false;
			this.flipPath = false;
			cleanGraph();
		}
	}
	
	/**
	 * Returns a route between two roads.
	 * @param to The road you want to navigate to.
	 * @param from The road you want to navigate from
	 * @param car If true, returns a car-friendly route. If false, returns a bike-friendly.
	 * @param fastest If true, return fastest path. If false, returns shortest. The fastest bike route does not make sense...
	 * @param point The mousepoint it should be closest to.
	 * @return The path.
	 * @throws NoPathFoundException Throws an exception if no road was found!
	 */
	public Road[] path(Road from, Road to, boolean car, boolean fastest, Point2D.Float point) throws NoPathFoundException {
		return path(from.closestVertex(point), to.closestVertex(point), car, fastest);
	}
	
	/**
	 * Returns a route between two vertex-objects.
	 * @param toVertex The vertex you want to navigate to.
	 * @param fromVertex The vertex you want to navigate from.
	 * @param car If true, returns a car-friendly route. If false, returns a bike-friendly.
	 * @param fastest If true, return fastest path. If false, returns shortest. The fastest bike route does not make sense...
	 * @return The path.
	 * @throws NoPathFoundException Throws an exception if no road was found!
	 */
	public Road[] path(Vertex fromVertex, Vertex toVertex, boolean car, boolean fastest) throws NoPathFoundException {
		if(fromVertex == null || toVertex == null) {throw new NoPathFoundException();}
		if(!car) {fastest=false;}
		
		if (!dragging || (dragging && marked.size() == 0)) {
			queue = new PriorityQueue<>();
			fromVertex.minDist(0f);
			queue.add(fromVertex);
			calculatePath(toVertex, car, fastest);
		} else if (!marked.contains(toVertex)) {
			calculatePath(toVertex, car, fastest);
		}
		Road[] path = finalPath(toVertex);
		if(!dragging) {cleanGraph();}
		if(path.length < 1) {throw new NoPathFoundException();}
		return path;
	}
	
	/**
	 * Implementation of Dijkstra's Algorithm with A* heuristics.
	 * @param target The algorithms stops once this vertex is reached.
	 * @param car True to only search roads where cars are allowed, false to only search where bikes are allowed.
	 * @param fastest True to find the fastest possible route. False to find the shortest.
	 * @throws NoPathFoundException Thrown if queue is ever empty.
	 */
	private void calculatePath(Vertex target, boolean car, boolean fastest) throws NoPathFoundException {
		Vertex current;
		while(!queue.isEmpty()) {
			current = queue.poll();
			
			if(marked.contains(current)) {continue;} else {marked.add(current);}
			
			//Check where all edges go, and relax
			for(Road edge : current.edges(car)) {
				Vertex relax = edge.otherVertex(current);
				if(relax(relax, edge, current, target, fastest)) {
					relax.edgeTo(edge);
					queue.add(relax);
				}
			}
			if(current == target) {return;}
		}
		//Empty queue means that no path could be found, clean and throw exception...
		if(!dragging) {cleanGraph();}
		throw new NoPathFoundException("Car: "+car+" ... Fastest: "+fastest);
	}
	
	/**
	 * Used by calculatePath - Dijkstra.
	 * @param vertex The vertex being relaxed.
	 * @param edge The edge (road), used for minDist.
	 * @param current The vertex being inspected right now.
	 * @param target The target vertex, used for heuristics.
	 * @param fastest True for fast-heuristics, false for short-heuristics
	 * @return Returns whether relaxation was successful.
	 */
	private boolean relax(Vertex vertex, Road edge, Vertex current, Vertex target, boolean fastest) {
		//Make sure to add them so we can 'clean' them after.
		visited.add(vertex);
		
		//Relax it.
		if(fastest) {
			if(vertex.minDist() > current.minDist() + edge.travelTime()) {
				vertex.minDist(current.minDist() + edge.travelTime());
				Point2D v = edge.vertexPosition(vertex);
				// (distance * 111323m) / (130km/t * (1000m/km) / (60min/time)) ca. = distance * 51.4
				vertex.heuristic(v.distance(target.getPosition()) * 51.4);
				return true;
			}
			return false;
		} else {
			if(vertex.minDist() > current.minDist() + edge.length()) {
				vertex.minDist(current.minDist() + edge.length());
				Point2D v = edge.vertexPosition(vertex);
				vertex.heuristic(v.distance(target.getPosition()) * Model.METER_CONVERSION);
				return true;
			}
			return false;
		}
	}

	/**
	 * Used to read the correct path.
	 * @param current The last vertex (goal).
	 * @return An array of all roads to visit in order to get from the last vertex to the first (NOTICE! Reversed order!)
	 */
	private Road[] finalPath(Vertex current) {
		List<Road> path = new ArrayList<>();
		
		// Manipulate the path (end walkpath), if needed.
		Vertex shared = null;
		Road[] r = null;
		Road endRoad = null, startRoad = null;
		if(end != null && start != null) { 
			if(end instanceof Address) {
				endRoad = ((Address) end).road();
			} else {
				endRoad = Main.model().closestRoad(end);
			}
			
			if(endRoad == current.edgeTo()) {
				shared = current.edgeTo().otherVertex(current);
			} else {
				shared = current;
			}
			r = endRoad.walkWay(end, shared.getPosition());
			path.add(r[0]);
			path.add(r[1]);
		}
		
		// Save the path
		while(current.edgeTo() != null) {
			path.add(current.edgeTo());
			current = current.edgeTo().otherVertex(current);
		}
		
		// Manipulate the path (start-walkpath), if needed.
		if(end != null && start != null) {
			if(start instanceof Address) {
				startRoad = ((Address) start).road();
			} else {
				startRoad = Main.model().closestRoad(start);
			}
			
			if(startRoad == path.get(path.size()-1)) {
				shared = path.get(path.size()-1).otherVertex(current);
			} else {
				shared = current;
			}
			r = startRoad.walkWay(start, shared.getPosition());
			path.add(r[1]);
			path.add(r[0]);
			
			
			path.remove(endRoad);
			path.remove(startRoad);
		}
		end = null; start = null;
		
		if(flipPath) {Collections.reverse(path);}
		return path.toArray(new Road[path.size()]);
	}
	
	/**
	 * "Clean" the graph after use, so it is ready for a new search.
	 */
	private void cleanGraph() {
		queue = new PriorityQueue<Vertex>();
		marked = new HashSet<Vertex>();
		for(Vertex v : visited) {
			v.reset();
		}
	}
	
	/**
	 * Returns the vertex at index (i) from the PathFinder.
	 * @param i Index
	 * @return Vertex or null.
	 */
	protected Vertex getVertex(int i) {
		if(i < 0 || i > vertices.length) {
			return null;
		}
		return vertices[i];
	}
	
	/**
	 * Makes it easier to translate vertex "id" into actual vertex.
	 * @param i id the ID or vertex index
	 * @return Vertex at index i or null.
	 */
	public static Vertex vertex(int i) {
		return Main.model().pathFinder().getVertex(i);
	}
}