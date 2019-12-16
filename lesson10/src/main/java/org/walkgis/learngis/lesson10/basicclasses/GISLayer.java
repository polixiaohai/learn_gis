package org.walkgis.learngis.lesson10.basicclasses;


import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;

public class GISLayer {
    public String name;
    public GISExtent extent;
    public Boolean drawAttributeOrNot = true;
    public int labelIndex = 2;
    public SHAPETYPE shapeType;
    public List<GISField> fields;
    public List<GISFeature> features = new ArrayList<GISFeature>();
    public List<GISFeature> selection = new ArrayList<>();


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

    public SelectResult select(GISVertex vertex, GISView view) {
        GISSelect select = new GISSelect();
        SelectResult selectResult = select.select(vertex, features, shapeType, view);
        if (selectResult == SelectResult.OK) {
            for (int i = 0; i < select.selectedFeatures.size(); i++) {
                if (!select.selectedFeatures.get(i).isSelected) {
                    select.selectedFeatures.get(i).isSelected = true;
                    selection.add(select.selectedFeatures.get(i));
                }
            }
        }

        return selectResult;
    }

    public void clearSelection() {
        selection.stream().forEach(sel -> sel.isSelected = false);
        selection.clear();
    }

    public void addFeature(GISFeature feature) {
        features.add(feature);
    }

    public int featureCount() {
        return features.size();
    }
}
