####Andrew Werner - 1/13/2014
## COS451 Final Project Report - Fortune's Algorithm
### Algorithm Overview

Fortune's algorithm is a sweepline algorithm for computing the Voronoi diagram on points in the plane.
A Voronoi Diagram of N sites in the plane is a partitioning of the plane into N cells where each cell contains one site and for all points in the cell, there is no site that is closer.

The main idea in the algorithm is derived from the defintion of a parabola as the set of points equidistant from a given point (the focus) and a line (the directrix). 
In this algorithm, our directrix is a horizonal sweepline that moves down the y-axis from above.

We recognize that all places where two parabolas meet must lie on edges of the Voronoi diagram and that all places where three parabolas meet must be a vertex in the diagram.

As the algorithm progresses, it maintains a wavefront of parabolas often termed the "beachfront" line. 

When we encounter a new site, we detect where the parabola it defines will intersect the existing beachfront (i.e. which arc it will break). In that instant, the parabola is really just a vertical line shooting up into the beachfront. As the sweepline continues, the two new breakpoints that this site is responsible for will separate from that initial impact point on the beachfront.  

When two breakpoints (boundaries points of the parabolic arcs) collide along the beachfront line a vertex is added to the diagram. Two breakpoints colliding represents the removal of an Arc from the beachfront.
This is called a CircleEvent because it means that three of the points define a circle that contains no other points. The center of the circle then must be a vertex in the Voronoi diagram.
These events are the only way vertices can be added to the diagram.

The events are added to the event queue and the algorithm follows handling events as described in [Computational Geometry: Algorithms & Applications](http://www.amazon.com/Computational-Geometry-Applications-Mark-Berg/dp/3642096816) on pages 157-158.

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
When I need BreakPoint successors, I retreive neighboring Arcs and examine their breakpoints.

BreakPoints are initialized with an ordered pair of sites. We know that a BreakPoint will only exist along the perpendicular bisector of the two sites. Their ordering as well as the y-values of the sites informs the location of the BreakPoint given the current sweepline location. BreakPoint locations are evaluated lazily when they are needed for looking at the ArcTree. BreakPoints also maintain HalfEdge information that points to the Voronoi edge in the diagram that it is a tracing part of. 

BreakPoints are stored in a HashSet. This is necessary because after the algorithm has consumed all of the events, some of the BreakPoints will not have been destroyed. These BreakPoints are on edges that go to infinity and must be finished.

### Code Organization

The Voronoi class takes an ArrayList of Points in its constructor and has a list of edges as a property.
Most of the heavy lifting is done in the Voronoi constructor with the two event-handling methods.
It uses these special purpose data-structures rather than a traditional doubly connected edge list because I didn't have a Java implementation of a DCEL handy.

### Degeneracies

The algorithm seems to handle most of the degenerate cases.
When multiple circle events occur at the same point, a 0-length edge is added to the diagram.
Vertical lines when the first two points have the same y-coordinate are handled correctly now.
I'm less certain about if there are many pointa all at the max y-value for the dataset.

### External Libraries

The only external library I use is the standard library from [Algorithms, 4th Edition](http://algs4.cs.princeton.edu/home/), the jar for which can be found in 3rd party.
Also, for my Point class, the ccw function was taken directly from the [Point2D class](http://algs4.cs.princeton.edu/12oop/Point2D.java.html) from Algs 4 and it is cited in the code too.

### Runtime

All operations in the processing of an event are O(log(N)) or faster.
There are at most a linear number of events (because there are only events for sites and the vertices of the Voronoi diagram but we also know that the number of faces in the diagram is N so from Euler's formula that the number of vertices is going to be some multiple of N).
Therefore we claim the algorithm should run in time O(Nlog(N)).

The empircal results ([here](/results.txt)) support this claim.
These runtimes were generated running on a Macbook Pro mid-2012 with 2.3 Ghz Intel Core i7 and 8Gb of RAM. 
The code was run with the option `-Xmx4G` allocating 4Gb of RAM for the JVM heap.
