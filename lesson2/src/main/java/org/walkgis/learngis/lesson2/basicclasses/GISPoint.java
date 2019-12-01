package org.walkgis.learngis.lesson3.basicclasses;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;

public class GISPoint extends GISSpatial{
    public GISPoint(GISVertex location) {
        this.center = location;
    }

    public double distance(GISVertex gisVertex) {
        return center.distance(gisVertex);
    }

    @Override
    public void draw(GraphicsContext graphicsContext) {
        graphicsContext.setFill(Color.RED);
        graphicsContext.fillArc(center.x, center.y, 6, 6, 0, 360, ArcType.OPEN);
    }
}
