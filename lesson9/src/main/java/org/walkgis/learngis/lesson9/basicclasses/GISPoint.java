package org.walkgis.learngis.lesson9.basicclasses;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
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
    public void draw(GraphicsContext graphicsContext, GISView gisView) {
        Point screenPoint = gisView.toScreenPoint(center);
        graphicsContext.setFill(Color.RED);
        graphicsContext.fillArc(screenPoint.x, screenPoint.y, 6, 6, 0, 360, ArcType.OPEN);
    }
}
