package org.walkgis.learngis.lesson10.basicclasses;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.awt.Point;
import java.util.List;

public class GISPolyline extends GISSpatial {
    public List<GISVertex> vertices;
    public double length;

    public GISPolyline(List<GISVertex> vertices) {
        this.vertices = vertices;
        this.center = GISTools.calculateCentroid(vertices);
        this.length = GISTools.calculateLength(vertices);
        this.extent = GISTools.calculateExtent(vertices);
    }

    @Override
    public void draw(GraphicsContext graphicsContext, GISView gisView, boolean isSelected) {
        List<Point> points = GISTools.getScreenPoints(vertices, gisView);

        graphicsContext.setStroke(isSelected ? GISConst.selectedLineColor : GISConst.lineColor);
        graphicsContext.setLineWidth(GISConst.lineWidth);

        double[] polygonsX = new double[points.size()], polygonsY = new double[points.size()];
        for (int i = 0, size = points.size(); i < size; i++) {
            polygonsX[i] = points.get(i).x;
            polygonsY[i] = points.get(i).y;
        }
        graphicsContext.strokePolyline(polygonsX, polygonsY, points.size());
    }

    public GISVertex fromNode() {
        if (vertices.size() == 0) return null;
        return vertices.get(0);
    }

    public GISVertex toNode() {
        if (vertices.size() == 0) return null;
        return vertices.get(vertices.size() - 1);
    }

    public double distance(GISVertex vertex) {
        double dis = Double.MAX_VALUE;
        for (int i = 0; i < vertices.size() - 1; i++) {
            dis = Math.min(GISTools.pointToSegment(vertices.get(i), vertices.get(i + 1), vertex), dis);
        }
        return dis;
    }
}
