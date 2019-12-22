package org.walkgis.learngis.lesson17.basicclasses;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GISLayer {
    public GISThematic thematic;
    public String name;
    public GISExtent extent;
    public Boolean drawAttributeOrNot = true;
    public boolean selectable = true;
    public boolean visible = true;
    public String path = "";
    public int labelIndex = 2;
    public SHAPETYPE shapeType;
    public List<GISField> fields;
    public List<GISFeature> features = new ArrayList<GISFeature>();
    public List<GISFeature> selection = new ArrayList<>();

    public GISLayer(String shpFilePath, SHAPETYPE shapeType, GISExtent extent, List<GISField> fields) {
        this.name = new File(shpFilePath).getName();
        this.path = shpFilePath;
        this.extent = extent;
        this.shapeType = shapeType;
        this.fields = fields;
        this.thematic = new GISThematic(shapeType);
    }

    public GISLayer(String shpFilePath, SHAPETYPE shapeType, GISExtent extent) {
        this.name = new File(shpFilePath).getName();
        this.path = shpFilePath;
        this.extent = extent;
        this.shapeType = shapeType;
        this.fields = new ArrayList<>();
        this.thematic = new GISThematic(shapeType);
    }

    public void draw(Graphics2D graphics2D, GISView view) {
        GISExtent extent = view.getRealExtent();
        for (int i = 0; i < features.size(); i++) {
            if (extent.insertectOrNot(features.get(i).spatial.extent))
                features.get(i).draw(graphics2D, view, drawAttributeOrNot, labelIndex, thematic);
        }
    }

    public void draw(Graphics2D graphics2D, GISView view, GISExtent displayExtent) {
        for (int i = 0; i < features.size(); i++) {
            if (displayExtent.insertectOrNot(features.get(i).spatial.extent))
                features.get(i).draw(graphics2D, view, drawAttributeOrNot, labelIndex, thematic);
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

    public SelectResult select(GISExtent extent) {
        GISSelect select = new GISSelect();
        SelectResult selectResult = select.select(extent, features);
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
        if (features.size() == 0) feature.id = 0;
        else feature.id = features.get(features.size() - 1).id + 1;
        features.add(feature);
    }

    public int featureCount() {
        return features.size();
    }

    public void addSelectedFeatureByID(int id) {
        GISFeature feature = getFeatureById(id);
        if (feature != null) {
            feature.isSelected = true;
            selection.add(feature);
        }
    }

    private GISFeature getFeatureById(int id) {
        for (GISFeature feature : features) {
            if (feature.id == id) return feature;
        }
        return null;
    }
}
