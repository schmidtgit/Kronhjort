package Model;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * A temporary piece of a coast that is used to connect the coasts.
 */
public class CoastlinePart extends Path2D.Float{
	private static final long serialVersionUID = 16052016L;
	private Point2D startPoint;

	/**
	 * Creates a CoastlinePart from a list of Point2D's.
	 * @param way The points of the coastline.
     */
	public CoastlinePart(List<Point2D> way){
		moveTo(way.get(0).getX(), way.get(0).getY());
		for(int i = 1; i<way.size(); i++) {
			lineTo(way.get(i).getX(), way.get(i).getY());
		}
		this.startPoint = way.get(0);
	}

	/**
	 * Returns the start point of the coast.
	 * @return The first point of the coast.
     */
	public Point2D startPoint(){
		return startPoint;
	}

	/**
	 * Inserts a new point as the start point and connects the original coast to it.
	 * @param x The x-coordinate for the new start point.
	 * @param y The y-coordinate for the new start point.
     */
	public void falseStart(float x, float y) {
		startPoint = new Point2D.Float(x, y);
		Path2D p = new Path2D.Float();
		p.append(getPathIterator(null), false);
		reset();
		moveTo(x, y);
		append(p.getPathIterator(null), true);
	}

	/**
	 * Checks if the coast is a complete cycle.
	 * @return True if the coasts starts and ends in the same point, otherwise false
     */
	public boolean complete(){
		return startPoint.equals(getCurrentPoint());
	}

	/**
	 * Converts the CoastlinePart into a PolygonApprox object
	 * @return The PolygonApprox equivalent to the CoastlinePart
     */
	public PolygonApprox toPolygonApprox(){
		List<Point2D> way = new ArrayList<>();
		PathIterator pi = getPathIterator(null);
		float[] storing = new float[2];
		while(!pi.isDone()){
			pi.currentSegment(storing);
			way.add(new Point2D.Float(storing[0], storing[1]));
			pi.next();
		}
		return new PolygonApprox(way);
	}
}
