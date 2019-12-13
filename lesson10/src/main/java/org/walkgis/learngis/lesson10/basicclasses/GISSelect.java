package org.walkgis.learngis.lesson10.basicclasses;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GISSelect {
    public List<GISFeature> selectedFeatures = new ArrayList<>();

    public SelectResult select(GISVertex vertex, List<GISFeature> features, SHAPETYPE shapetype, GISView view) {
        if (features.size() == 0) return SelectResult.EmptySet;
        selectedFeatures.clear();
        GISExtent minExtent = buildExtent(vertex, view);
        switch (shapetype) {
            case point:
                return selectPoint(vertex, features, view, minExtent);
            case polyline:
                return selectPolyline(vertex, features, view, minExtent);
            case polygon:
                return selectPolygon(vertex, features, view, minExtent);
            default:
                break;
        }
        return SelectResult.UnknownType;
    }

    private SelectResult selectPolygon(GISVertex vertex, List<GISFeature> features, GISView view, GISExtent minExtent) {
        selectedFeatures.clear();
        for (int i = 0; i < features.size(); i++) {
            if (!minExtent.insertectOrNot(features.get(i).spatial.extent)) continue;
            GISPolygon polygon = (GISPolygon) features.get(i).spatial;

            if (polygon.include(vertex)) {
                selectedFeatures.add(features.get(i));
            }
        }
        return (selectedFeatures.size() > 0) ? SelectResult.OK : SelectResult.TooFar;
    }

    private SelectResult selectPolyline(GISVertex vertex, List<GISFeature> features, GISView view, GISExtent minExtent) {
        Double distance = Double.MAX_VALUE;
        int id = -1;
        //筛选出屏幕视图范围内距离最近的要素
        for (int i = 0; i < features.size(); i++) {
            if (!minExtent.insertectOrNot(features.get(i).spatial.extent)) continue;
            GISLine line = (GISLine) features.get(i).spatial;

            double dist = line.distance(vertex);
            if (dist < distance) {
                distance = dist;
                id = i;
            }
        }
        if (id == -1) {
            selectedFeatures.clear();
            return SelectResult.TooFar;
        } else {
            //筛选出满足屏幕
            Double screenDis = view.toScreenDistance(distance);
            if (screenDis <= GISConst.minScreenDistance) {
                selectedFeatures.add(features.get(id));
                return SelectResult.OK;
            } else {
                selectedFeatures.clear();
                return SelectResult.TooFar;
            }
        }
    }

    private SelectResult selectPoint(GISVertex vertex, List<GISFeature> features, GISView view, GISExtent minExtent) {
        Double distance = Double.MAX_VALUE;
        int id = -1;
        //筛选出屏幕视图范围内距离最近的要素
        for (int i = 0; i < features.size(); i++) {
            GISFeature feature = features.get(i);
            if (!minExtent.insertectOrNot(feature.spatial.center)) continue;
            GISPoint point = (GISPoint) feature.spatial;

            double dist = point.distance(vertex);
            if (dist < distance) {
                distance = dist;
                id = i;
            }
        }
        if (id == -1) {
            selectedFeatures.clear();
            return SelectResult.TooFar;
        } else {
            //筛选出满足屏幕
            Double screenDis = view.toScreenDistance(vertex, features.get(id).spatial.center);
            if (screenDis <= GISConst.minScreenDistance) {
                selectedFeatures.add(features.get(id));
                return SelectResult.OK;
            } else {
                selectedFeatures.clear();
                return SelectResult.TooFar;
            }
        }
    }

    /**
     * 点转成5*5的空间矩形
     *
     * @param vertex
     * @param view
     * @return
     */
    private GISExtent buildExtent(GISVertex vertex, GISView view) {
        Point p0 = view.toScreenPoint(vertex);
        Point p1 = new Point(p0.x + (int) GISConst.minScreenDistance, p0.y + (int) GISConst.minScreenDistance);
        Point p2 = new Point(p0.x - (int) GISConst.minScreenDistance, p0.y - (int) GISConst.minScreenDistance);

        GISVertex g1 = view.toMapVertex(p1);
        GISVertex g2 = view.toMapVertex(p2);
        return new GISExtent(g1.x, g2.x, g1.y, g2.y);
    }
}
