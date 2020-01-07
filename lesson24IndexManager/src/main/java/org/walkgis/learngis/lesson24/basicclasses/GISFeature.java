package org.walkgis.learngis.lesson24.basicclasses;

import org.walkgis.learngis.lesson24.basicclasses.geometry.GISSpatial;

import java.awt.*;

public class GISFeature {
    public int id;
    public GISSpatial spatial;
    public GISAttribute attribute;
    public boolean isSelected = false;

    public GISFeature(GISSpatial spatial, GISAttribute attribute) {
        this.spatial = spatial;
        this.attribute = attribute;
    }

    public void draw(Graphics2D graphicsContext, GISView gisView, boolean drawAttributeOrNot, int index, GISThematic thematic) {
        spatial.draw(graphicsContext, gisView, isSelected, thematic);
        if (drawAttributeOrNot) {
            attribute.draw(graphicsContext, gisView, spatial.center, index);
        }
    }

    public Object getAttribute(int index) {
        return attribute.getValue(index);
    }
}
