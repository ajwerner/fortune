Implementation Report
---------------------

This document is to serve as the report for COS451, the class for which this code was implemented.

Fortune's algorithm is a sweepline algorithm for computing the Voronoi diagram on points in the plane.
A Voronoi Diagram of N sites in the plane is a partitioning of the plane into N cells where each cell contains one site and for all points in the cell, there is no site that is closer than that one point.

Except I was really hesitant to implement my own tree and have to deal with rebalancing etc as they suggest.
Instead I used a TreeSet of Arcs to represent the Beachfront line and a HashSet to store the BreakPoints.

It seems to be pretty fast and O(N*log(N)):
[timing results](results.txt)
