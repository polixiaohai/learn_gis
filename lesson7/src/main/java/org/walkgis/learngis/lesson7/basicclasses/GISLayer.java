package org.walkgis.learngis.lesson7.basicclasses;


import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;

public class GISLayer {
    public String name;
    public GISExtent extent;
    public Boolean drawAttributeOrNot;
    public int labelIndex;
    public SHAPETYPE shapeType;
    public List<GISField> fields;
    public List<GISFeature> features = new ArrayList<GISFeature>();

    public GISLayer(String name, SHAPETYPE shapeType, GISExtent extent, List<GISField> fields) {
        this.name = name;
        this.extent = extent;
        this.shapeType = shapeType;
        this.fields = fields;
    }

    public GISLayer(String name, SHAPETYPE shapeType, GISExtent extent) {
        this.name = name;
        this.extent = extent;
        this.shapeType = shapeType;
        this.fields = new ArrayList<>();

    }

    public GISLayer() {

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
