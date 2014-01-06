package org.ajwerner.voronoi;

import edu.princeton.cs.introcs.StdDraw;

/**
 * Created by ajwerner on 12/28/13.
 */
public class VoronoiEdge {
    public final Point site1, site2;
    public final double m, b; // parameters for line that the edge lies on
    public Point p1, p2;

    public VoronoiEdge(Point site1, Point site2) {
        this.site1 = site1;
        this.site2 = site2;

        m = -1.0 / ((site1.y - site2.y) / (site1.x - site2.x));
        Point midpoint = Point.midpoint(site1, site2);
        b = midpoint.y - m*midpoint.x;
    }

    public Point intersection(VoronoiEdge that) {
        double x = (that.b - this.b) / (this.m - that.m);
        double y = m * x + b;
        return new Point(x, y);
    }

    public void draw() {
        StdDraw.line((Voronoi.DEFAULT_MAX_DIM - b) / m, Voronoi.DEFAULT_MAX_DIM, -b / m, Voronoi.DEFAULT_MIN_DIM);
    }
}
