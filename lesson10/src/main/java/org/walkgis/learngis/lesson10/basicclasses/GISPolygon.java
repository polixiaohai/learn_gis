package org.walkgis.learngis.lesson10.basicclasses;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;

import java.awt.*;
import java.util.List;

public class GISPolygon extends GISSpatial {
    public List<GISVertex> vertexs;
    public double area;

    public GISPolygon(List<GISVertex> vertexs) {
        this.vertexs = vertexs;
        this.center = GISTools.calculateCentroid(vertexs);
        this.area = GISTools.calculateArea(vertexs);
        this.extent = GISTools.calculateExtent(vertexs);
    }

    @Override
    public void draw(GraphicsContext graphicsContext, GISView view) {
        List<Point> points = GISTools.getScreenPoints(vertexs, view);
        graphicsContext.setFill(Color.web("#2ecc71", 0.6));
        double[] polygonsX = new double[points.size()], polygonsY = new double[points.size()];
        for (int i = 0, size = points.size(); i < size; i++) {
            polygonsX[i] = points.get(i).x;
            polygonsY[i] = points.get(i).y;
        }
        graphicsContext.fillPolygon(polygonsX, polygonsY, points.size());
        graphicsContext.setStroke(Color.BLUE);
        graphicsContext.strokePolygon(polygonsX, polygonsY, points.size());
    }

    public boolean include(GISVertex vertex) {
        int count = 0;
        for (int i = 0; i < vertexs.size(); i++) {
            if (vertexs.get(i).isSame(vertex)) return false;
            int next = (i + 1) % vertexs.size();
            double minx = Math.min(vertexs.get(i).x, vertexs.get(next).x);
            double miny = Math.min(vertexs.get(i).y, vertexs.get(next).y);
            double maxx = Math.max(vertexs.get(i).x, vertexs.get(next).x);
            double maxy = Math.max(vertexs.get(i).y, vertexs.get(next).y);
            if (miny == maxy) {
                if (miny == vertex.y && vertex.x >= minx && vertex.x <= maxx) return false;
                else continue;
            }
            if (vertex.x > maxx || vertex.y > maxy || vertex.y < miny) continue;
            double x0 = vertexs.get(i).x + (vertex.y - vertexs.get(i).y) * (vertexs.get(next).x - vertexs.get(i).x) / vertexs.get(next).y - vertexs.get(i).y;
            if (x0 < vertex.x) continue;
            if (x0 == vertex.x) return false;
            if (vertex.y == miny) continue;
            count++;
        }
        return count % 2 != 0;
    }
}
