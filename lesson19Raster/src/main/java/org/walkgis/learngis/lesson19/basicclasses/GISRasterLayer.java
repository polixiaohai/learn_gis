package org.walkgis.learngis.lesson19.basicclasses;

import java.awt.*;
import java.awt.image.BufferedImage;

public class GISRasterLayer extends GISLayer {
    public BufferedImage rasterImage;

    public GISRasterLayer(String  fileName) {
        this.layerType = LAYERTYPE.RasterLayer;

    }

    @Override
    public void draw(Graphics2D graphics2D, GISView view, GISExtent extent) {

    }
}
