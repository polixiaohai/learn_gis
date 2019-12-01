package org.walkgis.learngis.lesson1.basicclasses;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;

public class GISPoint {
    public GISVertex location;
    public String attribute;

    public GISPoint(GISVertex location, String attribute) {
        this.location = location;
        this.attribute = attribute;
    }

    public void drawPoint(GraphicsContext graphicsContext) {
        graphicsContext.setFill(Color.RED);
        graphicsContext.fillArc(location.x, location.y, 3, 3, 0, 360, ArcType.OPEN);
    }

    public void drawAttribute(GraphicsContext graphicsContext) {
        graphicsContext.setFont(new Font("宋体", 20));
        graphicsContext.setFill(Color.BLUE);

        graphicsContext.fillText(attribute, (int) location.x, (int) location.y);
    }
}
