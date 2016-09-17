package Model;

import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Based on PolygonApprox class by Troels Bjerre Lund.
 */
public class PolygonApprox implements Shape, Serializable {
    private static final long serialVersionUID = 16052016L;
    protected float[] coords;
    protected float bx, by, bw, bh;
    protected int nPoints;

    protected PolygonApprox() {}

    public PolygonApprox(List<? extends Point2D> points) {
        nPoints = points.size();
        coords = new float[nPoints<<1];
        for (int i = 0 ; i < nPoints ; i++) {
            Point2D p = points.get(i);
            coords[(i << 1)] = (float)p.getX();
            coords[(i << 1)+1] = (float)p.getY();
        }
        init();
    }

    protected void init() {
        bx = coords[0];
        by = coords[1];
        bw = coords[0];
        bh = coords[1];
        for (int i = 2 ; i < coords.length ; i += 2) {
            bx = Math.min(bx, coords[i]);
            bw = Math.max(bw, coords[i]);
            by = Math.min(by, coords[i+1]);
            bh = Math.max(bh, coords[i+1]);
        }
        bw -= bx;
        bh -= by;
    }

    /**
     * Returns either the maximum x-value or maximum y-value of the shape.
     * @param x True if x, false if y.
     * @return Either maximum x-value or maximum y-value.
     */
    public double coord(boolean x) {
        if(x) {
            return getMaxX();
        }
        return getMaxY();
    }

    /**
     * Converts the PolygonApprox into list of Point2D.
     * @return
     */
    public List<Point2D> toList(){
        List<Point2D> way = new ArrayList<>();
        for(int i = 0; i < coords.length - 1 ; i += 2){
            way.add(new Point2D.Float(coords[i], coords[i+1]));
        }
        return way;
    }

    /**
     * Checks if this is a MultiPolygonApprox instead of using instanceOf.
     * @return Always false. This is used as a faster alternative to instanceOf.
     */
    public boolean isMultiPA(){
        return false;
    }

    /**
     * Returns the first point of the PolygonApprox.
     * @return
     */
    public Point2D startPoint(){ return new Point2D.Float(coords[0], coords[1]); }

    /**
     * Returns the point directly after the starting point, if it exists.
     * @return Start point if PolygonApprox consists of only one point.
     */
    public Point2D secondPoint() { 
    	if(nPoints == 1) { return startPoint(); }
    	else { return new Point2D.Float(coords[2], coords[3]); }
    }

    /**
     * Returns the last point of the PolygonApprox.
     * @return
     */
    public Point2D endPoint(){ return new Point2D.Float(coords[coords.length-2], coords[coords.length-1]); }

    /**
     * Returns the point directly before the last point, if it exists.
     * @return Last point if PolygonApprox consists of only one point.
     */
    public Point2D secondLastPoint() {
    	if(nPoints == 1) { return endPoint(); } 
    	else { return new Point2D.Float(coords[coords.length-4], coords[coords.length-3]); }
    }

    // Everything below this is from Troels' class

    public double distTo(Point2D p) {
        double dist = Double.MAX_VALUE;
        double px = p.getX();
        double py = p.getY();
        for (int i = 2 ; i < coords.length ; i += 2) {
            dist = Math.min(dist, Line2D.ptSegDist(coords[i-2], coords[i-1], coords[i], coords[i+1], px, py));
        }
        return dist;
    }

    public boolean contains(double _x, double _y) {
        throw new UnsupportedOperationException("Unexpected call to PolygonApprox.contains(double,double)");
    }

    public boolean contains(Point2D p) {
        throw new UnsupportedOperationException("Unexpected call to PolygonApprox.contains(Point2D)");
    }

    public boolean intersects(double x, double y, double w, double h) {
        throw new UnsupportedOperationException("Unexpected call to PolygonApprox.intersects(double,double,double,double)");
    }

    public boolean intersects(Rectangle2D r) {
        if (bx > r.getMaxX() ||
                by > r.getMaxY() ||
                bx + bw < r.getMinX() ||
                by + bh < r.getMinY()) return false;
        return true;
    }

    public boolean contains(double x, double y, double w, double h) {
        throw new UnsupportedOperationException("Unexpected call to PolygonApprox.contains(double,double,double,double)");
    }

    public boolean contains(Rectangle2D r) {
        throw new UnsupportedOperationException("Unexpected call to PolygonApprox.contains(Rectangle2D)");
    }

    public PathIterator getPathIterator(AffineTransform at) {
        if(at == null) {return  standardPathIterator();}
        float pixelsq = (float) (1/Math.abs(at.getDeterminant()));
        return getPathIterator(at, pixelsq);
    }

    private PathIterator standardPathIterator(){
        return new PolygonApproxIterator(new AffineTransform(), 0f);
    }

    public PathIterator getPathIterator(AffineTransform at, float pixelsq) {
        return new PolygonApproxIterator(at, pixelsq);
    }

    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return new PolygonApproxIterator(at, (float) (flatness * flatness));
    }

    protected class PolygonApproxIterator implements PathIterator {
        protected AffineTransform at;
        protected int index;
        protected float approx;

        public PolygonApproxIterator(AffineTransform _at, float _pixelsq) {
            at = _at;
            approx = _pixelsq;
        }

        public int getWindingRule() {
            return WIND_EVEN_ODD;
        }

        public boolean isDone() {
            return index >= coords.length;
        }

        protected float distSq(float x1, float y1, float x2, float y2) {
            float dx = x1 - x2;
            float dy = y1 - y2;
            return dx * dx + dy * dy;
        }

        public void next() {
            float fx = coords[index];
            float fy = coords[index+1];
            index += 2;
            while (index < coords.length - 2 &&
                    distSq(fx, fy, coords[index], coords[index+1]) < approx) index += 2;
        }

        public int currentSegment(float[] c) {
            if (isDone()) {
                throw new NoSuchElementException("poly approx iterator out of bounds");
            }
            int type;
            if (index == 0) {
                c[0] = coords[0];
                c[1] = coords[1];
                type = SEG_MOVETO;
            } else {
                c[0] = coords[index];
                c[1] = coords[index+1];
                type = SEG_LINETO;
            }
            if (at != null) {
                at.transform(c, 0, c, 0, 1);
            }
            return type;
        }

        public int currentSegment(double[] coords) {
            throw new UnsupportedOperationException("Unexpected call to PolygonApprox.currentSegment(double[])");
        }
    }

    public float getBiggestSide() {
        return Math.max(bw, bh);
    }

    public float getSize() {
        return bw * bw + bh * bh;
    }

    public float getMaxX() {
        return bx + bw;
    }

    public float getMinX() {
        return bx;
    }

    public float getMaxY() {
        return by + bh;
    }

    public float getMinY() {
        return by;
    }

    public float getCenterX() {
        return bx + bw / 2;
    }

    public float getCenterY() {
        return by + bh / 2;
    }

    public float getWidth() {
        return bw;
    }

    public float getHeight() {
        return bh;
    }

    public Rectangle getBounds() {
        return getBounds2D().getBounds();
    }

    public Rectangle2D getBounds2D() {
        return new Rectangle2D.Double(bx, by, bw, bh);
    }
}