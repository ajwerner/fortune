fortune
=======

Fortune's algorithm for Voronoi diagram creation implemented in Java.

This implementation is based on the description of the algorithm from [Computational Geometry: Algorithms & Applications](http://www.amazon.com/Computational-Geometry-Applications-Mark-Berg/dp/3642096816).
Except I was really hesitant to implement my own tree and have to deal with rebalancing etc as they suggest.
Instead I used a TreeSet of Arcs to represent the Beachfront line and a HashSet to store the BreakPoints.

It seems to be pretty fast and O(N*log(N)):
[timing results](results.txt)

Build
-----
```
ant
```

Run
---

to make it animate the construction of a Voronoi diagram over N random points in the unit square:

```java -jar dist/Voronoi.jar <N>```

to make it run a timing study:

```java -Xmx2G -jar dist/Voronoi.jar```

![screenshot](voronoi.png)

