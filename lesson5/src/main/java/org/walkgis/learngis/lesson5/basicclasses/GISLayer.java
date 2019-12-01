package org.walkgis.learngis.lesson5.basicclasses;


import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;

public class GISLayer {
    public String name;
    public GISExtent extent;
    public Boolean drawAttributeOrNot;
    public int labelIndex;
    public SHAPETYPE shapeType;
    List<GISFeature> features = new ArrayList<GISFeature>();

    public GISLayer(String _Name, SHAPETYPE _ShapeType, GISExtent _Extent) {
        name = _Name;
        shapeType = _ShapeType;
        extent = _Extent;
    }

    public void draw(GraphicsContext _Graphics, GISView _View) {
        for (int i = 0; i < features.size(); i++) {
            features.get(i).draw(_Graphics, _View, drawAttributeOrNot, labelIndex);
        }
    }

    public void addFeature(GISFeature feature) {
        features.add(feature);
    }

    public int featureCount() {
        return features.size();
    }
}
