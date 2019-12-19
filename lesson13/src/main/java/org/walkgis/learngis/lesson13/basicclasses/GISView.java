package org.walkgis.learngis.lesson13.basicclasses;

import javafx.scene.shape.Rectangle;

import java.awt.*;
import java.math.BigDecimal;

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

    public void updateExtent(GISMapAction action) {
        currentMapExtent.changeExtent(action);
        update(currentMapExtent, mapWindowSize);
    }

    public void updateExtent(GISExtent extent) {
        currentMapExtent.copyFrom(extent);
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
        this.scaleX = Math.max(scaleX, scaleY);
        this.scaleY = scaleX;

        this.mapW = this.mapWindowSize.getWidth() * scaleX;
        this.mapH = this.mapWindowSize.getHeight() * scaleY;

        GISVertex center = currentMapExtent.getCenter();
        this.mapMinX = center.x - mapW / 2;
        this.mapMinY = center.y - mapH / 2;

//        this.currentMapExtent = new GISExtent(new GISVertex(mapMinX, mapMinY), new GISVertex(mapMinX + mapW, mapMinY + mapH));
    }

    public Point toScreenPoint(GISVertex vertex) {
        double screenX = (vertex.x - mapMinX) / scaleX;
        double screenY = winH - (vertex.y - mapMinY) / scaleY;
        return new Point(
                new BigDecimal(screenX).setScale(0, BigDecimal.ROUND_HALF_UP).intValue(),
                new BigDecimal(screenY).setScale(0, BigDecimal.ROUND_HALF_UP).intValue()
        );
    }

    public GISVertex toMapVertex(Point point) {
        double mapX = scaleX * point.x + mapMinX;
        double mapY = scaleY * (winH - point.y) + mapMinY;
        return new GISVertex(mapX, mapY);
    }

    public Double toScreenDistance(GISVertex vertex, GISVertex center) {
        Point p1 = toScreenPoint(vertex);
        Point p2 = toScreenPoint(center);
        return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
    }

    public Double toScreenDistance(Double distance) {
        return toScreenDistance(new GISVertex(0, 0), new GISVertex(0, distance));
    }

    public void updateRectangle(Rectangle clientRectangle) {
        this.mapWindowSize = clientRectangle;
        update(currentMapExtent, mapWindowSize);
    }

    public GISExtent getRealExtent() {
        return new GISExtent(mapMinX, mapMinX + mapW, mapMinY, mapMinY + mapH);
    }

    public GISExtent rectToExtent(int x1, int x2, int y1, int y2) {
        GISVertex v1 = toMapVertex(new Point(x1, y1));
        GISVertex v2 = toMapVertex(new Point(x2, y2));
        return new GISExtent(v1.x, v2.x, v1.y, v2.y);
    }
}
