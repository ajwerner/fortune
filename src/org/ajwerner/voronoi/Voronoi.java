package org.ajwerner.voronoi;

import edu.princeton.cs.introcs.StdDraw;
import edu.princeton.cs.introcs.StdStats;
import edu.princeton.cs.introcs.Stopwatch;

import java.util.*;

/**
 * Created by ajwerner on 12/23/13.
 */
public class Voronoi {
    public static final double DEFAULT_MIN_DIM = -1.1;
    public static final double DEFAULT_MAX_DIM = 1.1;

    private double sweepLoc;
    private final ArrayList<Point> sites;
    private final ArrayList<VoronoiEdge> edgeList;
    private HashSet<BreakPoint> breakPoints;
    private TreeMap<ArcKey, CircleEvent> arcs;
    private TreeSet<Event> events;
    private double xMin, xMax, yMin, yMax;

    public double getSweepLoc() {
        return sweepLoc;
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            int N = Integer.parseInt(args[0]);
            ArrayList<Point> sites = new ArrayList<Point>();
            Random rnd = new Random();
            for (int i = 0; i < N; i++) {
                sites.add(new Point(rnd.nextDouble(), rnd.nextDouble()));
            }

            StdDraw.setCanvasSize(1024, 1024);

            Voronoi v = new Voronoi(sites, true);
            v.show();
        }
        else {
            int numTrials = 5;
            System.out.println("         N:   \ttime (s)");
            int[] Ns = {100000, 200000, 400000, 800000, 1600000, 3200000};
            for (int n : Ns) {
                double res[] = new double[numTrials];
                for (int i = 0; i < numTrials; i++) {
                    res[i] = randomTrial(n);
                }
                System.out.printf("%10d:\t%-5.6f +/- %f \n", n, StdStats.mean(res), StdStats.stddev(res)/Math.sqrt(numTrials));
            }
        }
    }

    private static double randomTrial(int N) {
        Random rnd = new Random();
        ArrayList<Point> sites = new ArrayList<Point>();
        Stopwatch s = new Stopwatch();
        double stop, start;
        sites.clear();
        for (int i = 0; i < N; i++) {
            sites.add(new Point(rnd.nextDouble(), rnd.nextDouble()));
        }
        start = s.elapsedTime();
        Voronoi v = new Voronoi(sites);
        stop = s.elapsedTime();

        return stop-start;
    }

    private void show() {
        StdDraw.clear();
        for (Point p : sites) {
            p.draw(StdDraw.RED);
        }
        for (VoronoiEdge e : edgeList) {
            if (e.p1 != null && e.p2 != null) {
                StdDraw.line(e.p1.x, e.p1.y, e.p2.x, e.p2.y);
            }
        }
        StdDraw.show();
    }

    private void draw() {
        StdDraw.clear();
        for (Point p : sites) {
            p.draw(StdDraw.RED);
        }
        for (BreakPoint bp : breakPoints) {
            bp.draw();
        }
        for (ArcKey a : arcs.keySet()) {
            ((Arc) a).draw();
        }
        for (VoronoiEdge e : edgeList) {
            if (e.p1 != null && e.p2 != null) {
                StdDraw.line(e.p1.x, e.p1.y, e.p2.x, e.p2.y);
            }
        }
        StdDraw.line(Voronoi.DEFAULT_MIN_DIM, sweepLoc, Voronoi.DEFAULT_MAX_DIM, sweepLoc);
        StdDraw.show(1);
    }

    public Voronoi(ArrayList<Point> sites) {
        this(sites, false);
    }

    public Voronoi(ArrayList<Point> sites, boolean animate) {
        // initialize data structures;
        this.sites = sites;
        edgeList = new ArrayList<VoronoiEdge>(sites.size());
        events = new TreeSet<Event>();
        breakPoints = new HashSet<BreakPoint>();
        arcs = new TreeMap<ArcKey, CircleEvent>();

        for (Point site : sites) {
            events.add(new Event(site));
        }
        sweepLoc = Voronoi.DEFAULT_MAX_DIM;
        do {
            if (animate) this.draw();
            Event cur = events.pollFirst();
            sweepLoc = cur.p.y;
            if (cur.getClass() == Event.class) {
                //Update bounding box maybe
                if (cur.p.x < xMin) xMin = cur.p.x;
                if (cur.p.x > xMax) xMax = cur.p.x;
                if (cur.p.y < yMin) yMin = cur.p.y;
                if (cur.p.y > yMax) yMax = cur.p.y;
                handleSiteEvent(cur);
            }
            else {
                CircleEvent ce = (CircleEvent) cur;
                handleCircleEvent(ce);
            }
        } while ((events.size() > 0));

        this.sweepLoc -= 2*Math.abs(yMax - yMin);
        for (BreakPoint bp : breakPoints) {
            bp.finish();
        }
    }

    private void handleSiteEvent(Event cur) {
        // Deal with first point case
        if (arcs.size() == 0) {
            arcs.put(new Arc(cur.p, this), null);
            return;
        }

        // Find the arc above the site
        Map.Entry<ArcKey, CircleEvent> arcEntryAbove = arcs.floorEntry(new ArcQuery(cur.p));
        Arc arcAbove = (Arc) arcEntryAbove.getKey();

        // Remove the circle event associated with this arc if there is one
        CircleEvent falseCE = arcEntryAbove.getValue();
        if (falseCE != null) {
            events.remove(falseCE);
        }

        BreakPoint breakL = arcAbove.left;
        BreakPoint breakR = arcAbove.right;
        VoronoiEdge newEdge = new VoronoiEdge(arcAbove.site, cur.p);
        this.edgeList.add(newEdge);
        BreakPoint newBreakL = new BreakPoint(arcAbove.site, cur.p, newEdge, true, this);
        BreakPoint newBreakR = new BreakPoint(cur.p, arcAbove.site, newEdge, false, this);
        breakPoints.add(newBreakL);
        breakPoints.add(newBreakR);

        Arc arcLeft = new Arc(breakL, newBreakL, this);
        Arc center = new Arc(newBreakL, newBreakR, this);
        Arc arcRight = new Arc(newBreakR, breakR, this);

        arcs.remove(arcAbove);
        arcs.put(arcLeft, null);
        arcs.put(center, null);
        arcs.put(arcRight, null);

        checkForCircleEvent(arcLeft);
        checkForCircleEvent(arcRight);
    }

    private void handleCircleEvent(CircleEvent ce) {
        Arc arcRight = (Arc) arcs.higherKey(ce.arc);
        Arc arcLeft = (Arc) arcs.lowerKey(ce.arc);
        if (arcRight != null) {
            CircleEvent falseCe = arcs.get(arcRight);
            if (falseCe != null) events.remove(falseCe);
            arcs.put(arcRight, null);
        }
        if (arcLeft != null) {
            CircleEvent falseCe = arcs.get(arcLeft);
            if (falseCe != null) events.remove(falseCe);
            arcs.put(arcLeft, null);
        }
        arcs.remove(ce.arc);

        ce.arc.left.finish(ce.vert);
        ce.arc.right.finish(ce.vert);

        breakPoints.remove(ce.arc.left);
        breakPoints.remove(ce.arc.right);

        VoronoiEdge e = new VoronoiEdge(ce.arc.left.s1, ce.arc.right.s2);
        edgeList.add(e);

        // Here we're trying to figure out if the org.ajwerner.voronoi.Voronoi vertex we've found is the left
        // or right point of the new edge.
        // If the edges being traces out by these two arcs take a right turn then we know
        // that the vertex is going to be above the current point
        boolean turnsLeft = Point.ccw(arcLeft.right.edgeBegin, ce.p, arcRight.left.edgeBegin) == 1;
        // So if it turns left, we know the next vertex will be below this vertex
        // so if it's below and the slow is negative then this vertex is the left point
        boolean isLeftPoint = (turnsLeft) ? (e.m < 0) : (e.m > 0);
        if (isLeftPoint) {
            e.p1 = ce.vert;
        }
        else {
            e.p2 = ce.vert;
        }
        BreakPoint newBP = new BreakPoint(ce.arc.left.s1, ce.arc.right.s2, e, !isLeftPoint, this);
        breakPoints.add(newBP);

        arcRight.left = newBP;
        arcLeft.right = newBP;

        checkForCircleEvent(arcLeft);
        checkForCircleEvent(arcRight);
    }

    private void checkForCircleEvent(Arc a) {
        Point circleCenter = a.checkCircle();
        if (circleCenter != null) {
            double radius = a.site.distanceTo(circleCenter);
            Point circleEventPoint = new Point(circleCenter.x, circleCenter.y - radius);
            CircleEvent ce = new CircleEvent(a, circleEventPoint, circleCenter);
            arcs.put(a, ce);
            events.add(ce);
        }
    }
}
