package org.walkgis.learngis.lesson17.basicclasses;

import java.awt.*;
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


    public GISVertex fromNode() {
        if (vertices.size() == 0) return null;
        return vertices.get(0);
    }

    public GISVertex toNode() {
        if (vertices.size() == 0) return null;
        return vertices.get(vertices.size() - 1);
    }

    public double distance(GISVertex vertex) {
        double dis = Double.POSITIVE_INFINITY;
        for (int i = 0; i < vertices.size() - 1; i++) {
            dis = Math.min(GISTools.pointToSegment(vertices.get(i), vertices.get(i + 1), vertex), dis);
        }
        return dis;
    }

    @Override
    public void draw(Graphics2D graphicsContext, GISView gisView, boolean isSelected, GISThematic thematic) {
        List<Point> points = GISTools.getScreenPoints(vertices, gisView);

        graphicsContext.setColor(isSelected ? GISConst.selectedLineColor : thematic.insideColor);
        graphicsContext.setStroke(new BasicStroke(thematic.size));

        int[] polygonsX = new int[points.size()], polygonsY = new int[points.size()];
        for (int i = 0, size = points.size(); i < size; i++) {
            polygonsX[i] = points.get(i).x;
            polygonsY[i] = points.get(i).y;
        }
        graphicsContext.drawPolyline(polygonsX, polygonsY, points.size());
    }
}
