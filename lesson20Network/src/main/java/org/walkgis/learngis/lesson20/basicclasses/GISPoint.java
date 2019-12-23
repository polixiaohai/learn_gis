package org.walkgis.learngis.lesson20.basicclasses;

import java.awt.*;

public class GISPoint extends GISSpatial {
    public GISPoint(GISVertex location) {
        this.center = location;
        this.extent = new GISExtent(location, location);
    }

    public double distance(GISVertex gisVertex) {
        return center.distance(gisVertex);
    }

    @Override
    public void draw(Graphics2D graphicsContext, GISView gisView, boolean isSelected, GISThematic thematic) {
        Point screenPoint = gisView.toScreenPoint(center);
        graphicsContext.setColor(isSelected ? GISConst.selectedPointColor : thematic.insideColor);
        graphicsContext.fillArc(
                screenPoint.x - thematic.size,
                screenPoint.y - thematic.size,
                thematic.size * 2,
                thematic.size * 2, 0, 360);
    }
}
