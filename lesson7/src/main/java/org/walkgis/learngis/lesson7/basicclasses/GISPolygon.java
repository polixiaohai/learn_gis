package org.walkgis.learngis.lesson7.basicclasses;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.awt.*;
import java.util.List;

public class GISPolygon extends GISSpatial {
    public List<GISVertex> vertices;
    public double area;

    public GISPolygon(List<GISVertex> vertices) {
        this.vertices = vertices;
        this.center = GISTools.calculateCentroid(vertices);
        this.area = GISTools.calculateArea(vertices);
        this.extent = GISTools.calculateExtent(vertices);
    }

    @Override
    public void draw(GraphicsContext graphicsContext, GISView view) {
        List<Point> points = GISTools.getScreenPoints(vertices, view);
        graphicsContext.setFill(Color.YELLOW);
        double[] polygonsX = new double[points.size()], polygonsY = new double[points.size()];
        for (int i = 0, size = points.size(); i < size; i++) {
            polygonsX[i] = points.get(i).x;
            polygonsY[i] = points.get(i).y;
        }
        graphicsContext.fillPolygon(polygonsX, polygonsY, points.size());
    }
}
