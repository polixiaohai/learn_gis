package org.walkgis.learngis.lesson12.basicclasses;

import java.awt.*;

public abstract class GISSpatial {
    public GISVertex center;
    public GISExtent extent;

    public abstract void draw(Graphics2D graphicsContext, GISView gisView, boolean isSelected);
}
