package org.walkgis.learngis.lesson10.basicclasses;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.ArcType;

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
    public void draw(GraphicsContext graphicsContext, GISView gisView, boolean isSelected) {
        Point screenPoint = gisView.toScreenPoint(center);
        graphicsContext.setFill(isSelected ? GISConst.selectedPointColor : GISConst.pointColor);
        graphicsContext.fillArc(
                screenPoint.x - GISConst.pointSize,
                screenPoint.y - GISConst.pointSize,
                GISConst.pointSize * 2,
                GISConst.pointSize * 2, 0, 360, ArcType.OPEN);
    }
}
