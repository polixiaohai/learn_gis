package org.walkgis.learngis.lesson7.basicclasses;

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

    public void copyFrom(GISVertex gisVertex) {
        this.x = gisVertex.x;
        this.y = gisVertex.y;
    }
}
