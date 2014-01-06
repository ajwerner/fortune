package org.ajwerner.voronoi;

import edu.princeton.cs.introcs.StdDraw;

/**
 * Created by ajwerner on 12/29/13.
 */
public class Parabola {
    private final double a, b, c;

    public Parabola(Point focus, double directrixY) {
        this.a = focus.x;
        this.b = focus.y;
        this.c = directrixY;
    }

    public void draw(double min, double max) {
        min = (min > -1.1) ? min : -1.1;
        max = (max < 1.1) ? max : 1.1;
        for (double x = min; x < max; x += .001) {
            double y = ((x-a)*(x-a) + (b*b) - (c*c)) / (2*(b-c));
            StdDraw.point(x, y);
        }
    }

    public void draw() {
        this.draw(0, 1);
    }
}
