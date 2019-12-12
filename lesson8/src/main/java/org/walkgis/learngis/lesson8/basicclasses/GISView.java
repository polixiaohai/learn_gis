package org.walkgis.learngis.lesson8.basicclasses;

import javafx.scene.shape.Rectangle;

import java.awt.*;

public class GISView {
    public GISExtent currentMapExtent;
    public Rectangle mapWindowSize;
    public double mapMinX, mapMinY;
    public int winW, winH;
    public double mapW, mapH;
    public double scaleX, scaleY;

    public GISView(GISExtent currentMapExtent, Rectangle mapWindowSize) {
        update(currentMapExtent, mapWindowSize);
    }

    public void changeView(GISMapAction action) {
        currentMapExtent.changeExtent(action);
        update(currentMapExtent, mapWindowSize);
    }

    public void update(GISExtent currentMapExtent, Rectangle mapWindowSize) {
        this.currentMapExtent = currentMapExtent;
        this.mapWindowSize = mapWindowSize;
        this.mapMinX = currentMapExtent.getMinX();
        this.mapMinY = currentMapExtent.getMinY();
        this.winW = (int) mapWindowSize.getWidth();
        this.winH = (int) mapWindowSize.getHeight();
        this.mapW = currentMapExtent.getWidth();
        this.mapH = currentMapExtent.getHeight();
        this.scaleX = mapW / winW;
        this.scaleY = mapH / winH;
    }

    public Point toScreenPoint(GISVertex gisVertex) {
        double screenX = (gisVertex.x - mapMinX) / scaleX;
        double screenY = (gisVertex.y - mapMinY) / scaleY;
        return new Point((int) screenX, (int) screenY);
    }

    public GISVertex toMapVertex(Point point) {
        double mapX = scaleX * point.x + mapMinX;
        double mapY = scaleY * point.y + mapMinY;
        return new GISVertex(mapX, mapY);
    }

    public void updateExtent(GISExtent extent) {
        currentMapExtent.copyFrom(extent);
        update(currentMapExtent, mapWindowSize);
    }
}
