package org.walkgis.learngis.lesson13.basicclasses;

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
    public void draw(Graphics2D graphicsContext, GISView gisView, boolean isSelected) {
        Point screenPoint = gisView.toScreenPoint(center);
        graphicsContext.setColor(isSelected ? GISConst.selectedPointColor : GISConst.pointColor);
        graphicsContext.fillArc(
                screenPoint.x - GISConst.pointSize,
                screenPoint.y - GISConst.pointSize,
                GISConst.pointSize * 2,
                GISConst.pointSize * 2, 0, 360);
    }
}
