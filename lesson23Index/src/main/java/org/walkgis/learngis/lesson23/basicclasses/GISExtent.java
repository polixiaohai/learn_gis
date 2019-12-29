package org.walkgis.learngis.lesson23.basicclasses;

public class GISExtent {
    public GISVertex bottomLeft;
    public GISVertex upRight;
    public Double area;
    private double zoomingFactor = 2;
    private double movingFactor = 0.25;

    public GISExtent(GISVertex bottomLeft, GISVertex upRight) {
        this.bottomLeft = bottomLeft;
        this.upRight = upRight;
        this.area = getWidth() * getHeight();
    }

    public GISExtent(double x1, double x2, double y1, double y2) {
        this.upRight = new GISVertex(Math.max(x1, x2), Math.max(y1, y2));
        this.bottomLeft = new GISVertex(Math.min(x1, x2), Math.min(y1, y2));
        this.area = getWidth() * getHeight();
    }

    public GISExtent(GISExtent extent) {
        this.upRight = new GISVertex(extent.upRight);
        this.bottomLeft = new GISVertex(extent.bottomLeft);
        this.area = getWidth() * getHeight();
    }

    public GISExtent(GISExtent e1, GISExtent e2) {
        this.upRight = new GISVertex(Math.max(e1.upRight.x, e2.upRight.x), Math.max(e1.upRight.y, e2.upRight.y));
        this.bottomLeft = new GISVertex(Math.min(e1.bottomLeft.x, e2.bottomLeft.x), Math.min(e1.bottomLeft.y, e2.bottomLeft.y));
        this.area = getWidth() * getHeight();
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
            case moveup:
                newMinY = getMinY() - getHeight() * movingFactor;
                newMaxY = getMaxY() - getHeight() * movingFactor;
                break;
            case movedown:
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
        this.area = getWidth() * getHeight();
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
        this.area = getWidth() * getHeight();
    }

    /**
     * 计算得到相交
     *
     * @param extent
     * @return
     */
    public boolean insertectOrNot(GISExtent extent) {
        return !(getMaxX() < extent.getMinX() || getMinX() > extent.getMaxX() || getMaxY() < extent.getMinY() || getMinY() > extent.getMaxY());
    }

    public boolean insertectOrNot(GISVertex point) {
        return !(getMaxX() < point.x || getMinX() > point.x || getMaxY() < point.y || getMinY() > point.y);
    }

    public GISVertex getCenter() {
        return new GISVertex((upRight.x + bottomLeft.x) / 2, (upRight.y + bottomLeft.y) / 2);
    }

    public boolean include(GISExtent extent) {
        return (getMaxX() >= extent.getMaxX() && getMinX() <= extent.getMinX() && getMaxY() >= extent.getMaxY() && getMinY() <= extent.getMinY());
    }

    public void merge(GISExtent extent) {
        if (extent == null) return;
        upRight.x = Math.max(upRight.x, extent.upRight.x);
        upRight.y = Math.max(upRight.y, extent.upRight.y);
        bottomLeft.x = Math.min(bottomLeft.x, extent.bottomLeft.x);
        bottomLeft.y = Math.min(bottomLeft.y, extent.bottomLeft.y);
        this.area = getWidth() * getHeight();
    }
}
