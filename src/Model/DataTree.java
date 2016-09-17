package Model;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

/**
 * The DataTree makes it easier to navigate through large quantities of data distributed across a 2D plane.
 * Based on the KD Tree data structure.
 */
public class DataTree implements Serializable {
	private static final long serialVersionUID = 16052016L;
	private static final float TOLERANCE = 0.00225f; // Tolerance factor for closestToPoint (225/111323) Tolerance ~225m
	private static final int STANDARD_LEAF_SIZE = 64;
	private Node root;
	private int leafSz;
	private List<LeafNode> allLeafs;
	
	/**
	 * Simple nodes in the data tree.
	 */
	class Node implements Serializable {
		private static final long serialVersionUID = 16052016L;
		private Node right;
		private Node left;
		private LeafNode leaf;
		private double value;
		public Node(PolygonApprox mp, boolean b) {value = mp.coord(b);}
		public double value() {return value;}
		public void right(Node r) {right = r;}
		public Node right() {return right;}
		public void left(Node l) {left = l;}
		public Node left() {return left;}
		public void leaf(LeafNode leaf) {this.leaf = leaf;}
		public LeafNode leaf() {return leaf;}
	}
	
	/**
	 * Special nodes responsible for holding references to the stored objects.
	 */
	class LeafNode implements Serializable {
		private static final long serialVersionUID = 16052016L;
		class sizeOrder implements Comparator<PolygonApprox>{
			public int compare(PolygonApprox a, PolygonApprox b) {
				return Float.compare(a.getSize(), b.getSize());
			}
		}
		public LeafNode(PolygonApprox[] o, int min, int max, boolean compress) {
			list = Arrays.copyOfRange(o, min, max+1);
			if(compress) {compress();} else {Arrays.sort(list, new sizeOrder().reversed());}
			required = new HashSet<Integer>();
			index = allLeafs.size();
			allLeafs.add(this);
		}

		public PolygonApprox[] list() {return list;}
		public int index() {return index;}
		public void required(HashSet<Integer> hm) {hm.addAll(required); hm.add(index);}
		public void required(LeafNode lf) {if(lf==this) {return;} required.add(lf.index());}
		private PolygonApprox[] list;
		private HashSet<Integer> required;
		private int index;
		private void compress() {
			List<PolygonApprox> comp = new ArrayList<>();
			List<PolygonApprox> leave = new ArrayList<>();
			for(PolygonApprox pa: list){
				if(pa.isMultiPA()){
					leave.add(pa);
				} else {
					comp.add(pa);
				}
			}
			if(comp.size() > 1) {
				List<List<Point2D>> compressedList = new ArrayList<>();
				for (PolygonApprox pa : comp) {
					compressedList.add(pa.toList());
				}
				MultiPolygonApprox compressed = new MultiPolygonApprox(compressedList);
				list = new PolygonApprox[leave.size() + 1];
				list[leave.size()] = compressed;
				for(int i = 0; i<leave.size(); i++){
					list[i] = leave.get(i);
				}
			}
		}
	}	
	
	@Deprecated
	public DataTree(List<? extends PolygonApprox> list) {
		this(list, STANDARD_LEAF_SIZE, false);
	}
	
	/**
	 * Creates a new DataTree from the given list.
	 * @param list The list of PolygonApprox subclasses to store in the DataTree.
	 * @param compress If true, the objects will be compressed into MultiPolygonApprox objects. They will be drawn the same, but all additional information will be lost.
	 */
	public DataTree(List<? extends PolygonApprox> list, boolean compress) {
		this(list, STANDARD_LEAF_SIZE, compress);
	}
	
	/**
	 * Creates a new DataTree from the given list.
	 * @param list The list of PolygonApprox subclasses to store in the DataTree.
	 * @param leafSz Specifies the desired size of each leaf node in the tree.
	 * @param compress If true, the objects will be compressed into MultiPolygonApprox objects. They will be drawn the same, but all additional information will be lost.
	 */
	public DataTree(List<? extends PolygonApprox> list, int leafSz, boolean compress) {
			if(list.size() < 1) {return;}
			if(leafSz<1) {leafSz = STANDARD_LEAF_SIZE;}
			
			allLeafs = new ArrayList<LeafNode>();
			this.leafSz = leafSz;
			PolygonApprox[] tmp = new PolygonApprox[list.size()];
			list.toArray(tmp);
			root = createNode(tmp, 0, tmp.length-1, true, compress);
			
			for(LeafNode nf : allLeafs) {
				setRequired(nf);
			}
	}

	/**
	 * Returns everything in the tree as a list of arrays of PolygonApprox objects.
	 * @return Might return an empty list, but never null.
	 */
	public List<PolygonApprox[]> tree(){
		if(root == null) {return new ArrayList<PolygonApprox[]>();}
		HashSet<Integer> hs = new HashSet<>();
		for(int i = 0; i < allLeafs.size(); i++) {
			hs.add(i);
		}
		return convertHashSet(hs);
	}

	/**
	 * Returns, at least, everything from the data tree within the given rectangle.
	 * @param box The desired map section expressed as a rectangle
	 * @return Might return an empty list, but never null.
	 */
	public List<PolygonApprox[]> tree(Rectangle2D box){
		if(root == null) {return new ArrayList<PolygonApprox[]>();}
		HashSet<Integer> set = new HashSet<>();
		addLeaf(set, root, box, true);
		return convertHashSet(set);
	}
	
	/**
	 * Returns the object closest to the given point within 225m (world space).
	 * @param p The point to compare against.
	 * @return The closest object. Returns null if nothing is found within 225m.
	 */
	public PolygonApprox closestToPoint(Point2D p) {
		ArrayList<PolygonApprox[]> list = (ArrayList<PolygonApprox[]>) tree(new Rectangle2D.Float((float) (p.getX()-(TOLERANCE /2)), (float) (p.getY()-(TOLERANCE /2)), TOLERANCE, TOLERANCE));
		
		double minDist = Double.MAX_VALUE;
		PolygonApprox closest = null;
		
		for(PolygonApprox[] mp : list) {
			for(PolygonApprox path : mp) {
				double tmp = distanceCalc(path, p);
				if(Double.compare(tmp, minDist) < 0) {
					minDist = tmp;
					closest = path;
				}
			}
		}
		if(minDist > TOLERANCE) {return null;}
		return closest;
	}
	
	/**
	 * Calculates the shortest distance between the given PolygonApprox and the given point.
	 * @param path The path to calculate the distance to.
	 * @param p The point in which the distance from the path will be calculated from.
	 * @return The distance in float.
	 */
	private float distanceCalc(PolygonApprox path, Point2D p) {
		PathIterator iterator = path.getPathIterator(null);
		float minDist = Float.MAX_VALUE; float currentDist;
		float[] coords = new float[2]; float[] prevCoords = new float[2];
		iterator.currentSegment(prevCoords); iterator.next();

		while(!iterator.isDone()) {
			iterator.currentSegment(coords);
			currentDist = (float) Math.abs(new Line2D.Float(prevCoords[0],prevCoords[1],coords[0],coords[1]).ptSegDist(p));
			if(Float.compare(currentDist, minDist) < 0) {minDist = currentDist;}
			prevCoords[0] = coords[0]; prevCoords[1] = coords[1];
			iterator.next();
		}
		return minDist;
	}
	
	/**
	 * Calculates if any objects from the given map section is required in any other map sections,
	 * and sets them as required.
	 * @param leaf
	 */
	private void setRequired(LeafNode leaf) {
		PolygonApprox[] map = leaf.list();
		for(PolygonApprox path : map) {
			PathIterator iterator = path.getPathIterator(null);
			float[] coords = new float[2];
			while(!iterator.isDone()) {
				iterator.currentSegment(coords);
				LeafNode nf = singleLeaf(root, coords[0], coords[1], true);
				if(nf!= null) {nf.required(leaf);}
				iterator.next();
			}
		}
	}
	
	/**
	 * A method that always only returns a single node. (Only used by setRequired(LeafNode leaf))
	 * @param current Used for recursion, start with root.
	 * @param valX The X coordinate used to find the correct map section.
	 * @param valY The Y coordinate used to find the correct map section.
	 * @param b Always call this with true, used for recursion.
	 * @return A single node where the point given belongs.
	 */
	private LeafNode singleLeaf(Node current, double valX, double valY, boolean b) {
		if(current.leaf() != null) {return current.leaf;}
		
		double value;
		if(b) {value = valX;} else {value = valY;}
		if(current.value() <= value && current.right() != null) {return singleLeaf(current.right(), valX, valY, !b);}
		if(current.value() >= value && current.left() != null) {return singleLeaf(current.left(), valX, valY, !b);}
		return null;
	}
	
	/**
	 * Converts leaf-nodes to a list of PolygonApprox arrays.
	 * @param set Indices of the desired leaf nodes. (Integers due to serialization).
	 * @return A list of PolygonApprox arrays.
	 */
	private ArrayList<PolygonApprox[]> convertHashSet(HashSet<Integer> set){
		ArrayList<PolygonApprox[]> list = new ArrayList<>();
		if(set.size() == 0) {
			list.add(new PolygonApprox[0]);
			return list;
		}

		for(Integer i : set) {
			list.add(allLeafs.get(i).list());
		}
		return list;
	}

	/**
	 * Creates a new node, and all the nodes below it.
	 * @param o The original list of PolygonApprox objects.
	 * @param min Used for recursion, always call it with 0.
	 * @param max Used for recursion, always call it with length-1
	 * @param b Used for recursion, always call it with true.
	 * @param compress True if the nodes should be compressed once they land in their final node.
	 * @return A node containing all the nodes below it.
	 */
	private Node createNode(PolygonApprox[] o, int min, int max, boolean b, boolean compress) {
		int delta = max-min;
		if(delta<leafSz) {
			Node r = new Node(o[0], b);
			r.leaf(new LeafNode(o, min, max, compress));
			return r;
		}
		
		Node r = new Node(lazySort(o, min, max, min+((max-min)/2),b), b);
		r.left(createNode(o,min, min+(delta/2), !b, compress));
		r.right(createNode(o,min+(delta/2)+1, max, !b, compress));
		return r;
	}
	
	/**
	 * LazySort partitions the given area of an array in larger/smaller than the "expected"-index object.
	 * @param o The original array to sort.
	 * @param min The minimum index of the area to sort.
	 * @param max The maximum index of the area to sort.
	 * @param expected Partitions from this point.
	 * @param b True sorts by x-coordinate, false sorts by y-coordinate.
	 * @return
	 */
	private PolygonApprox lazySort(PolygonApprox[] o, int min, int max, int expected, boolean b) {
		int right = max, left = min+1;
		if(min < 0 || expected < min || max < expected || o.length-1 < max ) {throw new IllegalArgumentException();}
		while(true){
			while(o[left].coord(b) <= o[min].coord(b)){
				if(left == max){break;} 
				left++;
			}
			
			while(o[right].coord(b) >= o[min].coord(b)){
				if(right == min){break;} 
				right--; 
				if(right < left){break;}
			}

			if(right < left){break;}
			
			if(o[left].coord(b)>o[right].coord(b)){swap(o, left, right);}
			else{break;}
		}
		swap(o, min, right);

		if(right == expected){return o[right];}
		if(right<expected){return lazySort(o,left,max, expected, b);}
		else{return lazySort(o,min, right, expected, b);}
	}
	
	/**
	 * Swaps two objects position in the given array.
	 * @param o The array to swap in the object in.
	 * @param a The first position to swap with.
	 * @param b The second position to swap with.
	 */
	private void swap(PolygonApprox[] o, int a, int b){
		PolygonApprox tmp = o[a];
		o[a] = o[b];
		o[b] = tmp;
	}
	
	/**
	 * Adds all needed leaf nodes to draw the given rectangle correct.
	 * @param set Used to keep track of all leafs.
	 * @param current Used for recursion, always call with root.
	 * @param box Defining the desired area.
	 * @param b Always call with true, used for recursion.
	 */
	private void addLeaf(HashSet<Integer> set, Node current, Rectangle2D box, Boolean b) {
		if(current.leaf() != null) {current.leaf.required(set); return;}
		
		double min = 0, max = 0;
		if(b) {min = box.getMinX(); max = box.getMaxX();} else {min = box.getMinY(); max = box.getMaxY();}
		if(current.value() <= max && current.right() != null) {addLeaf(set, current.right(), box, !b);}
		if(current.value() >= min && current.left() != null) {addLeaf(set, current.left(), box, !b);}
	}
}