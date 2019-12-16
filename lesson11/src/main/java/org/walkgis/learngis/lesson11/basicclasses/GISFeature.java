package org.walkgis.learngis.lesson11.basicclasses;

import javafx.scene.canvas.GraphicsContext;

public class GISFeature {
    public int id;
    public GISSpatial spatial;
    public GISAttribute attribute;
    public boolean isSelected = false;

    public GISFeature(GISSpatial spatial, GISAttribute attribute) {
        this.spatial = spatial;
        this.attribute = attribute;
    }

    public void draw(GraphicsContext graphicsContext, GISView gisView, boolean drawAttributeOrNot, int index) {
        spatial.draw(graphicsContext, gisView, isSelected);
        if (drawAttributeOrNot) {
            attribute.draw(graphicsContext, gisView, spatial.center, index);
        }
    }

    public Object getAttribute(int index) {
        return attribute.getValue(index);
    }
}
