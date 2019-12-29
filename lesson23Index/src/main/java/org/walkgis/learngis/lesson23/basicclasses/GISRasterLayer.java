package org.walkgis.learngis.lesson23.basicclasses;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

public class GISRasterLayer extends GISLayer {
    public BufferedImage rasterImage;

    public GISRasterLayer() {
        this.layerType = LAYERTYPE.RasterLayer;
    }

    public GISRasterLayer(String  fileName) {
        this.layerType = LAYERTYPE.RasterLayer;
    }

    @Override
    public void draw(Graphics2D graphics2D, GISView view, GISExtent extent) {

    }

    @Override
    public void write(DataOutputStream dataOutputStream) {

    }

    @Override
    public void read(DataInputStream dataInputStream) {
        this.name = new File(path).getName();
        try {
            this.visible = dataInputStream.readBoolean();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
