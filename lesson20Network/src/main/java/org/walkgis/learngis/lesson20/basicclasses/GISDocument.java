package org.walkgis.learngis.lesson20.basicclasses;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

    public GISLayer addLayer(GISLayer layer) {
        getUniqueName(layer);
        layers.add(layer);
        updateExtent();
        return layer;
    }

    public GISLayer addLayer(String absolutePath) {
        GISLayer layer = GISTools.getLayer(absolutePath);
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
        layers.forEach(lyr -> {
            if (lyr.layerType == LAYERTYPE.VectorLayer)
                ((GISVectorLayer) lyr).clearSelection();
        });
    }

    public SelectResult select(GISVertex v, GISView view) {
        SelectResult sr = SelectResult.TooFar;
        for (int i = 0; i < layers.size(); i++) {
            GISLayer lyr = layers.get(i);
            if (lyr.layerType == LAYERTYPE.VectorLayer) {
                GISVectorLayer layer = (GISVectorLayer) lyr;
                layer.clearSelection();
                if (layer.selectable) {
                    if (layer.select(v, view) == SelectResult.OK) {
                        sr = SelectResult.OK;
                    }
                }
            }
        }
        return sr;
    }

    public SelectResult select(GISExtent extent) {
        SelectResult sr = SelectResult.TooFar;
        for (int i = 0; i < layers.size(); i++) {
            GISLayer lyr = layers.get(i);
            if (lyr.layerType == LAYERTYPE.VectorLayer) {
                GISVectorLayer layer = (GISVectorLayer) lyr;
                if (layer.selectable) {
                    if (layer.select(extent) == SelectResult.OK) {
                        sr = SelectResult.OK;
                    }
                }
            }
        }
        return sr;
    }

    public void read(String documentFilePath) {
        layers.clear();
        BufferedInputStream inputStream = null;
        DataInputStream dataInputStream = null;
        try {
            inputStream = new BufferedInputStream(Files.newInputStream(Paths.get(documentFilePath), StandardOpenOption.READ));
            dataInputStream = new DataInputStream(inputStream);
            do {
                int layerSize = dataInputStream.readInt();
                for (int i = 0; i < layerSize; i++) {
                    String layerPath = dataInputStream.readUTF();
                    String ext = layerPath.substring(layerPath.lastIndexOf("."));
                    GISLayer layer = null;
                    if (ext.equalsIgnoreCase("." + GISConst.SHP)) {
                        layer = new GISVectorLayer();
                        layer.read(dataInputStream);
                        layer.path = layerPath;
                    } else if (ext.equalsIgnoreCase("." + GISConst.RASTER)) {
                        layer = new GISRasterLayer();
                        layer.read(dataInputStream);
                        layer.path = layerPath;
                    }
                    this.layers.add(layer);
                }
            } while (dataInputStream.read() != -1);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                dataInputStream.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(String documentFilePath) {
        BufferedOutputStream outputStream = null;
        DataOutputStream dataOutputStream = null;
        try {
            outputStream = new BufferedOutputStream(Files.newOutputStream(Paths.get(documentFilePath), StandardOpenOption.CREATE));
            dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeInt(layers.size());
            for (int i = 0; i < layers.size(); i++) {
                if (layers.get(i).path == null) return;
                GISLayer layer = layers.get(i);
                layer.write(dataOutputStream);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                dataOutputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
