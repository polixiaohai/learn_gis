package org.walkgis.learngis.lesson6.basicclasses;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.awt.Point;
import java.util.List;

public class GISLine extends GISSpatial {
    public List<GISVertex> allVertexs;
    public double length;

    public GISLine(List<GISVertex> allVertexs) {
        this.allVertexs = allVertexs;
        this.center = GISTools.calculateCentroid(allVertexs);
        this.length = GISTools.calculateLength(allVertexs);
        this.extent = GISTools.calculateExtent(allVertexs);
    }

    @Override
    public void draw(GraphicsContext graphicsContext, GISView gisView) {
        List<Point> points = GISTools.getScreenPoints(allVertexs, gisView);

        graphicsContext.setFill(Color.RED);
        double[] polygonsX = new double[points.size()], polygonsY = new double[points.size()];
        for (int i = 0, size = points.size(); i < size; i++) {
            polygonsX[i] = points.get(i).x;
            polygonsY[i] = points.get(i).y;
        }
        graphicsContext.strokePolyline(polygonsX, polygonsY, points.size());
    }

    public GISVertex fromNode() {
        if (allVertexs.size() == 0) return null;
        return allVertexs.get(0);
    }

    public GISVertex toNode() {
        if (allVertexs.size() == 0) return null;
        return allVertexs.get(allVertexs.size() - 1);
    }
}
