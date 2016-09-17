package View;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import Model.Road;

/**
 * Used to define a Direction for the NavigationList.
 */
public class Direction {
	private String dirText, arrow;
	private int length;
	private Point2D intersection;
	private String nextRoadName;

	/**
	 * Initializes a Direction from the two given Road objects.
	 * @param from Road objects from the path based on the input in the AddressFinder.
	 * @param to Road objects from the path based on the input in the AddressFinder.
	 * @param length The length of the 'from' Road, which is used to define length to next direction.
     */
	public Direction(Road from, Road to, int length) {
		if(!to.name().trim().equals("") && !to.name().equals(null)) {
			nextRoadName = to.name();
		} else
			nextRoadName = "unnamed road";
		this.length = roundLength(length);
		dirText = findTurnType(from, to);
		intersection = from.sharedVertex(to).getPosition();
	}

	/**
	 * Initializes Direction for last direction.
	 * @param destination A Point2D, which is used to define the location of the destination for zooming.
	 * @param length The length of the last Road object before the actual destination.
     */
	public Direction(Point2D destination, int length) {
		this.length = roundLength(length);
		dirText = "You will arrive at your destination, in " + length + "m";
		arrow = "finish";
		intersection = destination;
	}

	/**
	 * Initializes Direction for roundabouts.
	 * @param intersection Define the location of the roundabout for zooming.
	 * @param to The next Road object after the roundabout.
	 * @param exitsBefore The number of non-roundabout Road objects before the actual exit.
     * @param length The length of the from Road, which defines the distance to the roundabout.
     */
	public Direction(Point2D intersection, Road to, int exitsBefore, int length) {
		if(!to.name().trim().equals("") && !to.name().equals(null)) {
			nextRoadName = to.name();
		} else
			nextRoadName = "unnamed road";
		arrow = "roundabout";
		this.length = roundLength(length);
		dirText = "In " + this.length + "m, take " + exitsBefore + ". exit onto " + nextRoadName;
		this.intersection = intersection;
	}

	/**
	 * Returns the dirText, which is the Direction in text.
	 * @return
     */
	public String toString() { return dirText; }

	/**
	 * Returns a String representing the orientation of the arrow in the NavigationList.
	 * @return
     */
	public String arrow() { return arrow; }

	/**
	 * Returns the length of the Road object before the interaction.
	 * @return
     */
	public int length() { return length; }

	/**
	 * Returns the location of the Vertex between the two Road objects.
	 * @return
     */
	public Point2D intersection() { return intersection; }

	/**
	 * Returns the rounded length for readable directions.
	 * @param length
	 * @return
     */
	private int roundLength(int length) {
		int result = 0;
		if(length < 25) { result = length; }
		else if(length < 500) { result = (length / 25) * 25; }
		else if(length < 1000) { result = (length / 50) * 50; }
		else { result = (length / 100) * 100; }
		return result;
	}

	/**
	 * Determines the direction text in reference to the two Road objects.
	 * @param source The Road object from the path, from which one comes from.
	 * @param next The Road object from the path, to which one travels to.
     * @return dirText, which describes the direction - left, right or continue.
     */
	public String findTurnType(Road source, Road next) {
		Model.Vertex sharedVertex = source.sharedVertex(next);
		Road[] edges = null;
		if (sharedVertex != null) {
			edges = sharedVertex.edges(true);
		}
		ArrayList<Road> adjRoads = new ArrayList<>();
		adjRoads.add(next);
		for(Road edge : edges) {
			if((angle(source, edge)%360 == 0 && !source.equals(edge) && source.name().equals(edge.name()) || (angle(next, edge)%360 == 0 && !next.equals(edge) && next.name().equals(edge.name())))) {
			} else {
				adjRoads.add(edge);
			}
		}
		if(adjRoads.contains(source) && adjRoads.size() > 1) { adjRoads.remove(source); }

		if(adjRoads.size() == 1) { arrow = "forward"; return "Continue along " + nextRoadName + " in " + length + "m"; }
		else if(adjRoads.size() == 0) { return "Dead end?"; }
		else {
			int nextAngle = angle(source, next);
			int[] angles = new int[adjRoads.size()];
			ArrayList<Road> forwardRoads = new ArrayList<>();
			int splitPoint = 180;
			for(int i = 0; i < adjRoads.size(); i++) {
				int angle = angle(source, adjRoads.get(i));
				if(angle > 135 && angle < 225) { forwardRoads.add(adjRoads.get(i)); }
				angles[i] = angle;
			}
			Arrays.sort(angles);
			if(forwardRoads.size() == 1) {
				if(next.equals(forwardRoads.get(0))) { arrow = "forward"; return "Continue along " + nextRoadName + " in " + length + "m"; }
				else { splitPoint = angle(source, forwardRoads.get(0)); }
			} else if(forwardRoads.size() > 1) {
				int closestAngle = 180;
				for(int i = 0; i<forwardRoads.size(); i++) {
					int tempAngle = Math.abs(180 - angle(source, forwardRoads.get(i)));
					if(tempAngle < closestAngle) {
						closestAngle = tempAngle;
						splitPoint = angle(source, forwardRoads.get(i));
					}
				}
				if(splitPoint == nextAngle) { arrow = "forward"; return "Continue along " + nextRoadName + " in " + length + "m"; }
			}
			if(nextAngle < splitPoint) {
				int j = 0;
				while(angles[j] < splitPoint && angles[j] < nextAngle && j < angles.length) {
					j++;
				}
				if(j == 0) { arrow = "right"; return "Turn right onto " + nextRoadName + " in " + length + "m"; }
				else if (j == 1) { arrow = "right"; return "Turn right onto " + nextRoadName + ", second road, in " + length + "m"; }
				else if (j == 2) { arrow = "right"; return "Turn right onto " + nextRoadName + ", third road, in " + length + "m"; }
				else { arrow = "right"; return "Turn right onto " + nextRoadName + ", " + (j+1) + "th road, in " + length + "m"; }
			} else {
				int j = angles.length-1;
				while(angles[j] > splitPoint && angles[j] > nextAngle && j > 0) {
					j--;
				}
				if(j == angles.length-1) { arrow = "left"; return "Turn left onto " + nextRoadName + " in " + length + "m"; }
				else if (j == angles.length-2) { arrow = "left"; return "Turn left onto " + nextRoadName + ", second road, in " + length + "m"; }
				else if (j == angles.length-3) { arrow = "left"; return "Turn left onto " + nextRoadName + ", third road, in " + length + "m"; }
				else { arrow = "left"; return "Turn left onto " + nextRoadName + ", " + (angles.length - j) + "th road, in " + length + "m"; }
			}
		}
	}

	/**
	 * Determines the angle between two Road objects.
	 * @param source The Road object from which the angle should be calculated from.
	 * @param next The other Road object which is used to calculate the angle.
     * @return Returned in degrees.
	 * @throws NullPointerException In case of null a NullPointer is thrown.
     */
	public static int angle(Road source, Road next) throws NullPointerException {
		Model.Vertex sharedVertex = source.sharedVertex(next);
		Point2D anchor = null;
		if (sharedVertex != null) {
			anchor = sharedVertex.getPosition();
		}
		Point2D nextPoint = next.secondPosition(next.sharedVertex(source));
		Point2D sourcePoint = source.secondPosition(source.sharedVertex(next));

		double angle1 = Math.atan2(nextPoint.getY() - anchor.getY(), nextPoint.getX() - anchor.getX());
		double angle2 = Math.atan2(sourcePoint.getY() - anchor.getY(), sourcePoint.getX() - anchor.getX());

		int result = (int) Math.toDegrees(angle2 - angle1);
		if (result < 0) {
			result += 360;
		}
		return result;

	}
}