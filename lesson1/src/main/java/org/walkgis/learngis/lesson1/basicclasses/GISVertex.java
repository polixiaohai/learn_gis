package org.walkgis.learngis.lesson1.basicclasses;

public class GISVertex {
    public double x;
    public double y;

    public GISVertex() {
    }

    public GISVertex(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double distance(GISVertex anotherVertex) {
        return Math.sqrt((x - anotherVertex.x) * (x - anotherVertex.x) + (y - anotherVertex.y) * (y - anotherVertex.y));
    }
}
