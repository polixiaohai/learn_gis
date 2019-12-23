package org.walkgis.learngis.lesson12.basicclasses;

import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class GISTools {
    public static List<Point> getScreenPoints(List<GISVertex> vertices, GISView view) {
        return vertices.stream().map(vertex -> view.toScreenPoint(vertex)).collect(Collectors.toList());
    }

    public static GISVertex calculateCentroid(List<GISVertex> vertices) {
        if (vertices.size() == 0) return null;
        AtomicReference<Double> x = new AtomicReference<>((double) 0);
        AtomicReference<Double> y = new AtomicReference<>((double) 0);
        vertices.forEach(vertex -> {
            x.updateAndGet(v -> v + vertex.x);
            y.updateAndGet(v -> v + vertex.y);
        });

        return new GISVertex(x.get() / vertices.size(), y.get() / vertices.size());
    }

    public static GISExtent calculateExtent(List<GISVertex> vertices) {
        if (vertices.size() == 0) return null;
        double minx = Double.POSITIVE_INFINITY;
        double miny = Double.POSITIVE_INFINITY;
        double maxx = Double.NEGATIVE_INFINITY;
        double maxy = Double.NEGATIVE_INFINITY;
        for (GISVertex vertex : vertices) {
            minx = Math.min(vertex.x, minx);
            miny = Math.min(vertex.y, miny);
            maxx = Math.max(vertex.x, maxx);
            maxy = Math.max(vertex.y, maxy);
        }
        return new GISExtent(new GISVertex(minx, miny), new GISVertex(maxx, maxy));
    }

    public static double calculateLength(List<GISVertex> vertices) {
        double length = 0;
        if (vertices.size() <= 1) return length;
        for (int i = 0, size = vertices.size() - 1; i < size; i++) {
            length += vertices.get(i).distance(vertices.get(i + 1));
        }
        return length;
    }

    public static double calculateArea(List<GISVertex> vertices) {
        double area = 0;
        if (vertices.size() <= 2) return area;
        for (int i = 0, size = vertices.size() - 1; i < size; i++) {
            area += vectorProduct(vertices.get(i), vertices.get(i + 1));
        }
        area += vectorProduct(vertices.get(vertices.size() - 1), vertices.get(0));
        return area / 2;
    }

    private static double vectorProduct(GISVertex v1, GISVertex v2) {
        return v1.x * v2.y - v1.y * v2.x;
    }

    public static double pointToSegment(GISVertex a, GISVertex b, GISVertex c) {
        double dot1 = dot3Product(a, b, c);
        if (dot1 > 0) return b.distance(c);
        double dot2 = dot3Product(b, a, c);
        if (dot2 > 0) return a.distance(c);
        double dist = cross3Product(a, b, c) / a.distance(b);
        return Math.abs(dist);
    }

    private static double cross3Product(GISVertex a, GISVertex b, GISVertex c) {
        GISVertex ab = new GISVertex(b.x - a.x, b.y - a.y);
        GISVertex ac = new GISVertex(c.x - a.x, c.y - a.y);
        return vectorProduct(ab, ac);
    }

    private static double dot3Product(GISVertex a, GISVertex b, GISVertex c) {
        GISVertex ab = new GISVertex(b.x - a.x, b.y - a.y);
        GISVertex bc = new GISVertex(c.x - b.x, c.y - b.y);
        return ab.x * bc.x + ab.y * bc.y;
    }
}
