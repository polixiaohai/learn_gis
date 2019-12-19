package org.walkgis.learngis.lesson14.basicclasses;

import java.awt.*;
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
}
