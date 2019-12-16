package org.walkgis.learngis.lesson12.basicclasses;

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
    public void draw(Graphics2D graphicsContext, GISView view, boolean isSelected) {
        List<Point> points = GISTools.getScreenPoints(vertexs, view);

        graphicsContext.setColor(GISConst.polygonFillColor);
        int[] polygonsX = new int[points.size()], polygonsY = new int[points.size()];
        for (int i = 0, size = points.size(); i < size; i++) {
            polygonsX[i] = points.get(i).x;
            polygonsY[i] = points.get(i).y;
        }
        graphicsContext.fillPolygon(polygonsX, polygonsY, points.size());

        graphicsContext.setColor(isSelected ? GISConst.selectedPolygonBoundaryColor : GISConst.polygonBoundaryColor);
        graphicsContext.setStroke(new BasicStroke(GISConst.polygonBoundaryWidth));
        graphicsContext.drawPolygon(polygonsX, polygonsY, points.size());
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

    public boolean include2(GISVertex vertex) {
        int cross = 0;
        for (int i = 0, count = vertexs.size(); i < count; i++) {
            if (vertexs.get(i).isSame(vertex)) return false;

            GISVertex p1 = vertexs.get(i);
            GISVertex p2 = vertexs.get((i + 1) % count);

            if (p1.y == p2.y) continue;
            if (vertex.y < min(p1.y, p2.y)) continue;
            if (vertex.y >= max(p1.y, p2.y)) continue;
            double x = (vertex.y - p1.y) * (p2.x - p1.x) / (p2.y - p1.y) + p1.x;

            // 只统计p1p2与p向右射线的交点
            if (x > vertex.x) {
                cross++;
            }
        }
        return cross % 2 == 1;
    }

    private double max(double left, double right) {
        if (left < right) return right;
        else if (left > right) return left;
        else return 0;
    }

    private double min(double left, double right) {
        if (left < right) return left;
        else if (left > right) return right;
        else return 0;
    }
}
