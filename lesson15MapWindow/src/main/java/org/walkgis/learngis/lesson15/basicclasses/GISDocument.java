package org.walkgis.learngis.lesson15.basicclasses;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GISDocument {
    public List<GISLayer> layers = new ArrayList<>();
    public GISExtent extent;

    public GISLayer getLayer(String layerName) {
        for (GISLayer layer : layers) {
            if (layer.name.equalsIgnoreCase(layerName)) return layer;
        }
        return null;
    }

    public GISLayer addLayer(String absolutePath) {
        GISLayer layer = new GISShapefile().readShapefile(absolutePath);
        layer.path = absolutePath;
        getUniqueName(layer);
        layers.add(layer);
        updateExtent();
        return layer;
    }

    private void updateExtent() {
        extent = null;
        if (layers.size() == 0) return;
        extent = new GISExtent(layers.get(0).extent);
        for (int i = 0; i < layers.size(); i++)
            extent.merge(layers.get(i).extent);
    }

    private void getUniqueName(GISLayer layer) {
        List<String> names = new ArrayList<>();
        for (int i = 0, count = layers.size(); i < count; i++)
            names.add(layers.get(i).name);
        Collections.sort(names);
        for (int i = 0; i < names.size(); i++)
            if (layer.name == names.get(i))
                layer.name = names.get(i) + "1";
    }

    public void removeLayer(String layerName) {
        layers.remove(getLayer(layerName));
        updateExtent();
    }

    public void draw(Graphics2D graphics2D, GISView view) {
        if (layers.size() == 0) return;
        GISExtent displayExtent = view.getRealExtent();
        for (int i = 0; i < layers.size(); i++) {
            GISLayer layer = layers.get(i);
            if (layer.visible)
                layer.draw(graphics2D, view, displayExtent);
        }

    }

    public void switchLayer(String selectedName, String upperName) {
        GISLayer layer1 = getLayer(selectedName);
        GISLayer layer2 = getLayer(upperName);

        int index1 = layers.indexOf(layer1);
        int index2 = layers.indexOf(layer2);
        layers.set(index1, layer2);
        layers.set(index2, layer1);

    }

    public boolean isEmpty() {
        return layers.size() == 0;
    }

    public void clearSelection() {
        layers.forEach(lyr -> lyr.clearSelection());
    }

    public SelectResult select(GISVertex v, GISView view) {
        SelectResult sr = SelectResult.TooFar;
        for (int i = 0; i < layers.size(); i++) {
            if (layers.get(i).selectable) {
                if (layers.get(i).select(v, view) == SelectResult.OK) {
                    sr = SelectResult.OK;
                }
            }
        }
        return sr;
    }

    public SelectResult select(GISExtent extent) {
        SelectResult sr = SelectResult.TooFar;
        for (int i = 0; i < layers.size(); i++) {
            if (layers.get(i).selectable) {
                if (layers.get(i).select(extent) == SelectResult.OK) {
                    sr = SelectResult.OK;
                }
            }
        }
        return sr;
    }

    public void read(String documentFilePath) {
        layers.clear();
        try {
            FileInputStream fsr = new FileInputStream(new File(documentFilePath));
            DataInputStream bw = new DataInputStream(fsr);

            String path = GISTools.readString(bw);
            GISLayer layer = addLayer(path);
            layer.path = path;
            layer.drawAttributeOrNot = bw.readBoolean();
            layer.labelIndex = bw.readInt();
            layer.selectable = bw.readBoolean();
            layer.visible = bw.readBoolean();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void write(String documentFilePath) {
        try {
            FileOutputStream fsr = new FileOutputStream(new File(documentFilePath));
            DataOutputStream bw = new DataOutputStream(fsr);
            for (int i = 0; i < layers.size(); i++) {
                GISLayer layer = layers.get(i);
                GISTools.writeString(layer.path, bw);
                bw.writeBoolean(layer.drawAttributeOrNot);
                bw.writeInt(layer.labelIndex);
                bw.writeBoolean(layer.selectable);
                bw.writeBoolean(layer.visible);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
