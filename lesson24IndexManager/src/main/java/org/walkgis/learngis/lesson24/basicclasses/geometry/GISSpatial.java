package org.walkgis.learngis.lesson24.basicclasses.geometry;

import org.walkgis.learngis.lesson24.basicclasses.GISExtent;
import org.walkgis.learngis.lesson24.basicclasses.GISThematic;
import org.walkgis.learngis.lesson24.basicclasses.GISVertex;
import org.walkgis.learngis.lesson24.basicclasses.GISView;

import java.awt.*;

public abstract class GISSpatial {
    public GISVertex center;
    public GISExtent extent;

    public abstract void draw(Graphics2D graphicsContext, GISView gisView, boolean isSelected, GISThematic thematic);
}
