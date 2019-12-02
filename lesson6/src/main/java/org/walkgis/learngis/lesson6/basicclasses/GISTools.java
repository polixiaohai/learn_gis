package org.walkgis.learngis.lesson6.basicclasses;

import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class GISTools {
    public static GISVertex calculateCentroid(List<GISVertex> vertices) {
        if (vertices.size() == 0) return null;
        AtomicReference<Double> x = new AtomicReference<>((double) 0);
        AtomicReference<Double> y = new AtomicReference<>((double) 0);
        vertices.forEach(vertex -> {
            x.updateAndGet(v -> new Double((double) (v + vertex.x)));
            y.updateAndGet(v -> new Double((double) (v + vertex.y)));
        });

        return new GISVertex(x.get() / vertices.size(), y.get() / vertices.size());
    }

    public static GISExtent calculateExtent(List<GISVertex> vertices) {
        if (vertices.size() == 0) return null;
        final double[] minx = {Double.MAX_VALUE};
        final double[] miny = {Double.MAX_VALUE};
        final double[] maxx = {Double.MIN_VALUE};
        final double[] maxy = {Double.MIN_VALUE};
        vertices.forEach(vertex -> {
            if (vertex.x < minx[0]) minx[0] = vertex.x;
            if (vertex.x > maxx[0]) maxx[0] = vertex.x;
            if (vertex.y < miny[0]) miny[0] = vertex.y;
            if (vertex.y > maxy[0]) maxy[0] = vertex.y;
        });
        return new GISExtent(minx[0], maxx[0], miny[0], maxy[0]);
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

    public static List<Point> getScreenPoints(List<GISVertex> vertices, GISView view) {
        return vertices.stream().map(vertex -> view.toScreenPoint(vertex)).collect(Collectors.toList());
    }
}
