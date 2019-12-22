package org.walkgis.learngis.lesson18.basicclasses;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GISLayer {
    public THEMATICTYPE thematictype;
    public HashMap<Object, GISThematic> thematics = new HashMap<>();
    public int thematicIndex;
    public String name;
    public GISExtent extent;
    public Boolean drawAttributeOrNot = true;
    public boolean selectable = true;
    public boolean visible = true;
    public String path = "";
    public int labelIndex = 0;
    public SHAPETYPE shapeType;
    public List<GISField> fields;
    public List<GISFeature> features = new ArrayList<GISFeature>();
    public List<GISFeature> selection = new ArrayList<>();
    public List<Integer> levelIndexs = new ArrayList<>();

    public GISLayer(String shpFilePath, SHAPETYPE shapeType, GISExtent extent, List<GISField> fields) {
        this.name = new File(shpFilePath).getName();
        this.path = shpFilePath;
        this.extent = extent;
        this.shapeType = shapeType;
        this.fields = fields;

        makeUnifiedValueMap();
    }

    public GISLayer(String shpFilePath, SHAPETYPE shapeType, GISExtent extent) {
        this.name = new File(shpFilePath).getName();
        this.path = shpFilePath;
        this.extent = extent;
        this.shapeType = shapeType;
        this.fields = new ArrayList<>();
        makeUnifiedValueMap();
    }

    public void draw(Graphics2D graphics2D, GISView view, GISExtent extent) {
        extent = (extent == null) ? view.getRealExtent() : extent;
        if (thematictype == THEMATICTYPE.UnifiedValue) {
            GISThematic thematic = thematics.get(thematictype);
            for (int i = 0; i < features.size(); i++) {
                if (extent.insertectOrNot(features.get(i).spatial.extent))
                    features.get(i).draw(graphics2D, view, drawAttributeOrNot, labelIndex, thematic);
            }
        } else if (thematictype == THEMATICTYPE.UniqueValue) {
            for (int i = 0; i < features.size(); i++) {
                GISThematic thematic = thematics.get(features.get(i).getAttribute(thematicIndex));
                if (extent.insertectOrNot(features.get(i).spatial.extent))
                    features.get(i).draw(graphics2D, view, drawAttributeOrNot, labelIndex, thematic);
            }
        } else if (thematictype == THEMATICTYPE.GradualColor) {
            for (int i = 0; i < features.size(); i++) {
                GISThematic thematic = thematics.get(levelIndexs.get(i));
                if (extent.insertectOrNot(features.get(i).spatial.extent))
                    features.get(i).draw(graphics2D, view, drawAttributeOrNot, labelIndex, thematic);
            }
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

    public void makeUniqueValueMap(int fieldIndex) {
        //修改专题图地图样式
        thematictype = THEMATICTYPE.UniqueValue;
        thematicIndex = fieldIndex;
        List<Object> values = new ArrayList<>();
        for (int i = 0; i < features.size(); i++) {
            values.add(features.get(i).getAttribute(thematicIndex));
        }

        List<Object> uniqueValues = GISTools.findUniqueVlaues(values);
        Color outsideColor = null;
        int size = 0;
        for (GISThematic thematic : thematics.values()) {
            outsideColor = thematic.outsideColor;
            size = thematic.size;
            break;
        }

        thematics.clear();
        for (Object obj : values) {
            GISThematic thematic = new GISThematic(outsideColor, size, GISTools.getRandomColor());
            thematics.put(obj, thematic);
        }
    }

    public void makeUnifiedValueMap() {
        thematictype = THEMATICTYPE.UnifiedValue;
        thematics.clear();
        thematics.put(thematictype, new GISThematic(shapeType));
    }

    public boolean makeGradualColor(int fieldIndex, int levelNumber) {
        List<Double> values = new ArrayList<>();

        try {
            for (int i = 0; i < features.size(); i++) {
                values.add(Double.parseDouble(features.get(i).getAttribute(thematicIndex).toString()));
            }
        } catch (Exception ex) {
            return false;
        }

        thematictype = THEMATICTYPE.GradualColor;
        thematicIndex = fieldIndex;
        List<Double> levels = GISTools.findLevels(values, levelNumber);

        levelIndexs.clear();
        for (int i = 0; i < features.size(); i++) {
            int levelIndex = GISTools.whichLevel(levels, Double.parseDouble(features.get(i).getAttribute(thematicIndex).toString()));
            levelIndexs.add(levelIndex);
        }
        Color outSideColor = Color.BLACK;
        int size = 0;
        for (GISThematic thematic : thematics.values()) {
            outSideColor = thematic.outsideColor;
            size = thematic.size;
            break;
        }

        thematics.clear();
        for (int i = 0; i < levelNumber; i++) {
            thematics.put(i, new GISThematic(outSideColor, size, GISTools.getGradualColor(i, levelNumber)));
        }
        return true;
    }
}
