package org.walkgis.learngis.lesson24.basicclasses;

import org.walkgis.learngis.lesson24.basicclasses.enums.LAYERTYPE;
import org.walkgis.learngis.lesson24.basicclasses.enums.SHAPETYPE;
import org.walkgis.learngis.lesson24.basicclasses.enums.SelectResult;
import org.walkgis.learngis.lesson24.basicclasses.enums.THEMATICTYPE;
import org.walkgis.learngis.lesson24.basicclasses.index.RTree;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GISVectorLayer extends GISLayer {
    public THEMATICTYPE thematictype;
    public HashMap<Object, GISThematic> thematics = new HashMap<>();
    public int thematicIndex;
    public Boolean drawAttributeOrNot = false;
    public boolean selectable = true;
    public int labelIndex = 0;
    public SHAPETYPE shapeType;
    public List<GISField> fields;
    public List<GISFeature> features = new ArrayList<GISFeature>();
    public List<GISFeature> selection = new ArrayList<>();
    public List<Integer> levelIndexs = new ArrayList<>();
    public RTree rTree;

    public GISVectorLayer() {
        this.layerType = LAYERTYPE.VectorLayer;
    }

    public GISVectorLayer(String shpFilePath, SHAPETYPE shapeType, GISExtent extent, List<GISField> fields, boolean needIndex) {
        this.name = new File(shpFilePath).getName();
        this.path = shpFilePath;
        this.extent = extent;
        this.fields = fields;
        this.shapeType = shapeType;
        this.layerType = LAYERTYPE.VectorLayer;
        makeUnifiedValueMap();
        if (needIndex)
            rTree = new RTree(this);
    }


    public GISVectorLayer(String shpFilePath, SHAPETYPE shapeType, GISExtent extent, List<GISField> fields) {
        this(shpFilePath, shapeType, extent, fields, true);
        makeUnifiedValueMap();
    }

    public GISVectorLayer(String shpFilePath, SHAPETYPE shapeType, GISExtent extent) {
        this(shpFilePath, shapeType, extent, new ArrayList<>(), true);
        makeUnifiedValueMap();
    }

    public void buildRTree() {
        rTree = new RTree(this);
        for (int i = 0; i < features.size(); i++)
            rTree.insertData(i);
        extent = new GISExtent(rTree.root.mbr);
    }

    public void updateExtent() {
        if (features.size() == 0) extent = null;
        else {
            extent = new GISExtent(features.get(0).spatial.extent);
            for (int i = 0; i < features.size(); i++) {
                extent.merge(features.get(i).spatial.extent);
            }
        }
    }

    public void draw(Graphics2D graphics2D, GISView view, GISExtent extent) {
        extent = (extent == null) ? view.getRealExtent() : extent;
        if (rTree == null)
            drawFeatures(graphics2D, features, view, extent);
        else
            drawFeatures(graphics2D, rTree.query(extent, false), view, extent);
    }

    private void drawFeatures(Graphics2D graphics2D, List<GISFeature> query, GISView view, GISExtent extent) {
        if (thematictype == THEMATICTYPE.UnifiedValue) {
            GISThematic thematic = thematics.get(thematictype);
            for (int i = 0; i < features.size(); i++) {
                if (rTree == null && !extent.insertectOrNot(features.get(i).spatial.extent)) continue;
                features.get(i).draw(graphics2D, view, drawAttributeOrNot, labelIndex, thematic);
            }
        } else if (thematictype == THEMATICTYPE.UniqueValue) {
            for (int i = 0; i < features.size(); i++) {
                if (rTree == null && !extent.insertectOrNot(features.get(i).spatial.extent)) continue;
                GISThematic thematic = thematics.get(features.get(i).getAttribute(thematicIndex));
                features.get(i).draw(graphics2D, view, drawAttributeOrNot, labelIndex, thematic);
            }
        } else if (thematictype == THEMATICTYPE.GradualColor) {
            for (int i = 0; i < features.size(); i++) {
                if (rTree == null && !extent.insertectOrNot(features.get(i).spatial.extent)) continue;
                GISThematic thematic = thematics.get(levelIndexs.get(i));
                features.get(i).draw(graphics2D, view, drawAttributeOrNot, labelIndex, thematic);
            }
        }
    }

    @Override
    public void write(DataOutputStream dataOutputStream) {
        try {
            dataOutputStream.writeUTF(this.path);

            dataOutputStream.writeBoolean(this.visible);
            dataOutputStream.writeBoolean(this.drawAttributeOrNot);
            dataOutputStream.writeInt(this.labelIndex);
            dataOutputStream.writeBoolean(this.selectable);
            GISShapefile gisShapefile = new GISShapefile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void read(DataInputStream dataInputStream) {
        try {
            this.name = new File(path).getName();
            this.visible = dataInputStream.readBoolean();
            this.drawAttributeOrNot = dataInputStream.readBoolean();
            this.labelIndex = dataInputStream.readInt();
            this.selectable = dataInputStream.readBoolean();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public SelectResult select(GISVertex vertex, GISView view) {
        GISSelect select = new GISSelect(this);
        SelectResult selectResult = select.selectByVertex(vertex, view);
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
        GISSelect select = new GISSelect(this);
        SelectResult selectResult = select.selectByExtent(extent);
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

    public void select(List<GISFeature> features) {
        features.forEach(f -> {
            f.isSelected = true;
            selection.add(f);
        });
    }

    public void clearSelection() {
        selection.stream().forEach(sel -> sel.isSelected = false);
        selection.clear();
    }

    public void addFeature(GISFeature feature, boolean updateExtent) {
        if (features.size() == 0) feature.id = 0;
        else feature.id = features.get(features.size() - 1).id + 1;
        features.add(feature);
        if (rTree != null) rTree.insertData(features.size() - 1);
        if (!updateExtent) return;
        if (features.size() == 1)
            extent = new GISExtent(feature.spatial.extent);
        else
            extent.merge(feature.spatial.extent);
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

    public void deleteAllFeatures() {
        features.clear();
        extent = null;
        if (this.rTree != null) rTree = new RTree(this);
    }

    public void deleteFeatureByIndex(int featureIndex) {
        features.remove(featureIndex);
        if (rTree != null) buildRTree();
    }

    public void deleteFeature(GISFeature feature) {
        int index = findFeatureIndex(feature);
        deleteFeatureByIndex(index);
    }

    private int findFeatureIndex(GISFeature feature) {
        return findFeatureIndexById(feature.id);
    }

    public void deleteFeatureById(int id) {
        int index = findFeatureIndexById(id);
        deleteFeatureByIndex(index);
    }

    private int findFeatureIndexById(int id) {
        for (int i = 0; i < features.size(); i++)
            if (features.get(i).id == id)
                return i;
        return -1;
    }
}
