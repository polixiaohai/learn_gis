package org.walkgis.learngis.lesson3.basicclasses;

import java.util.Map;

public class GISExtent {
    public GISVertex bottomLeft;
    public GISVertex upRight;

    public GISExtent(GISVertex bottomLeft, GISVertex upRight) {
        this.bottomLeft = bottomLeft;
        this.upRight = upRight;
    }

    public GISExtent(double x1, double x2, double y1, double y2) {
        this.upRight = new GISVertex(Math.max(x1, x2), Math.max(y1, y2));
        this.bottomLeft = new GISVertex(Math.min(x1, x2), Math.min(y1, y2));
    }

    public double getMinX() {
        return bottomLeft.x;
    }

    public double getMinY() {
        return bottomLeft.y;
    }

    public double getMaxX() {
        return upRight.x;
    }

    public double getMaxY() {
        return upRight.y;
    }

    public double getWidth() {
        return upRight.x - bottomLeft.x;
    }

    public double getHeight() {
        return upRight.y - bottomLeft.y;
    }

}
