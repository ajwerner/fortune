# Report

This document is to serve as the report for COS451, the class for which this code was implemented.

### Algorithm Overview

Fortune's algorithm is a sweepline algorithm for computing the Voronoi diagram on points in the plane.
A Voronoi Diagram of N sites in the plane is a partitioning of the plane into N cells where each cell contains one site and for all points in the cell, there is no site that is closer.

The main idea in the algorithm is derived from the defintion of a parabola as the set of points equidistant from a given point (the focus) and a line (the directrix). 
In this algorithm, our directrix is a horizonal sweepline that moves down the y-axis from above.

We recognize that all places where two parabolas meet must lie on edges of the Voronoi diagram and that all places where three parabolas meet must be a vertex in the diagram.

As the algorithm progresses, it maintains a wavefront of parabolas often termed the "beachfront" line. 

When two breakpoints collide along the beachfront line a vertex is added to the diagram. 
This is called a CircleEvent because 

### Data Structures

Events are stored in a TreeSet that offers O(log(N)) polling, adding, and removing of points.
Events all have a point at which they occur. 
CircleEvents also encode an Arc and a circle center point.

Traditionally the algorithm stores both BreakPoints and Arcs in a single tree where the BreakPoints are the interior nodes and the Arcs are the leaves. 
Partly because I don't trust myself to efficiently rebalance my own tree, I decided to depart from this general design.
The tree is mostly used to locate the leaf Arc that is directly above a new point (an also to find successors of BreakPoints).
I instead implemented a tree of Arcs where each Arc contains pointers to the BreakPoints on its left and right (if they exists).
The nice observation is that the tree is only used to retreive the Arc above a given point.
Arcs can thus be viewed as non-overlapping intervals on the x-axis.
Arcs are stored in a TreeSet<ArcKey> where I made an abstract class called an ArcKey so that I could use different comparators when storing the Arcs in the tree and when looking for the Arc that contains a give new point.
When I look for the Arc containing a new Point p I create an ArcQuery(p) and then use the handy floorEntry method on the tree. 
When I need successors BreakPoints, I retreive neighboring Arcs and examine their breakpoints.

BreakPoints represent an ordered tuple of sites, they are stored in a HashSet.
BreakPoints also represent half edges.

### Code Organization

The Voronoi class takes an ArrayList of Points in its constructor and has a list of edges as a property.
Most of the heavy lifting is done in the Voronoi constructor with the two event-handling methods.
It uses these special purpose data-structures rather than a traditional doubly connected edge list because I didn't have a Java implementation of a DCEL handy.

### Degeneracies

The algorithm seems to handle the degenerate cases.
When multiple circle events occur at the same point, a 0-length edge is added to the diagram.

Vertical lines, particularly when the first two points have the same y-coordinate are also handled correctly now.

### External Libraries

The only external library I use is the standard library from [Algorithms, 4th Edition](http://algs4.cs.princeton.edu/home/), the jar for which can be found in 3rd party.
Also, for my Point class, the ccw function was taken directly from the [Point2D class](http://algs4.cs.princeton.edu/12oop/Point2D.java.html) from Algs 4 and it is cited in the code too.

### Runtime

All operations in the processing of an event are O(log(N)) or faster.
There are at most a linear number of events (because there are only events for sites and the vertices of the Voronoi diagram but we also know that the number of faces in the diagram is N so from Euler's formula that the number of vertices is going to be some multiple of N).
Therefore we claim the algorithm should run in time O(Nlog(N)).

The empircal results ([here](/results.txt)) support this claim.

[timing results](results.txt)
