package org.walkgis.learngis.lesson8.basicclasses;

public class GISExtent {
    public GISVertex bottomLeft;
    public GISVertex upRight;
    private double zoomingFactor = 2;
    private double movingFactor = 0.25;

    public GISExtent(GISVertex bottomLeft, GISVertex upRight) {
        this.bottomLeft = bottomLeft;
        this.upRight = upRight;
    }

    public GISExtent(double x1, double x2, double y1, double y2) {
        this.upRight = new GISVertex(Math.max(x1, x2), Math.max(y1, y2));
        this.bottomLeft = new GISVertex(Math.min(x1, x2), Math.min(y1, y2));
    }

    public void changeExtent(GISMapAction action) {
        double newMinX = bottomLeft.x, newMinY = bottomLeft.y, newMaxX = upRight.x, newMaxY = upRight.y;
        switch (action) {
            case zoomin:
                newMinX = ((getMinX() + getMaxX()) - getWidth() / zoomingFactor) / 2;
                newMinY = ((getMinY() + getMaxY()) - getHeight() / zoomingFactor) / 2;
                newMaxX = ((getMinX() + getMaxX()) + getWidth() / zoomingFactor) / 2;
                newMaxY = ((getMinY() + getMaxY()) + getHeight() / zoomingFactor) / 2;
                break;
            case zoomout:
                newMinX = ((getMinX() + getMaxX()) - getWidth() * zoomingFactor) / 2;
                newMinY = ((getMinY() + getMaxY()) - getHeight() * zoomingFactor) / 2;
                newMaxX = ((getMinX() + getMaxX()) + getWidth() * zoomingFactor) / 2;
                newMaxY = ((getMinY() + getMaxY()) + getHeight() * zoomingFactor) / 2;
                break;
            case movedown:
                newMinY = getMinY() - getHeight() * movingFactor;
                newMaxY = getMaxY() - getHeight() * movingFactor;
                break;
            case moveup:
                newMinY = getMinY() + getHeight() * movingFactor;
                newMaxY = getMaxY() + getHeight() * movingFactor;
                break;
            case movelet:
                newMinX = getMinX() + getWidth() * movingFactor;
                newMaxX = getMaxX() + getWidth() * movingFactor;
                break;
            case moveright:
                newMinX = getMinX() - getWidth() * movingFactor;
                newMaxX = getMaxX() - getWidth() * movingFactor;
                break;
            default:
                break;
        }

        upRight.x = newMaxX;
        upRight.y = newMaxY;
        bottomLeft.x = newMinX;
        bottomLeft.y = newMinY;
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

    public void copyFrom(GISExtent extent) {
        upRight.copyFrom(extent.upRight);
        bottomLeft.copyFrom(extent.bottomLeft);
    }
}
