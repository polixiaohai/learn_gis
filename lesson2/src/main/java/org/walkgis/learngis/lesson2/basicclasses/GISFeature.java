package org.walkgis.learngis.lesson2.basicclasses;

import javafx.scene.canvas.GraphicsContext;

public class GISFeature {
    public GISSpatial spatial;
    public GISAttribute attribute;

    public GISFeature(GISSpatial spatial, GISAttribute attribute) {
        this.spatial = spatial;
        this.attribute = attribute;
    }

    public void draw(GraphicsContext graphicsContext, boolean drawAttributeOrNot, int index) {
        spatial.draw(graphicsContext);
        if (drawAttributeOrNot) {
            attribute.draw(graphicsContext, spatial.center, index);
        }
    }

    public Object getAttribute(int index) {
        return attribute.getValue(index);
    }
}
