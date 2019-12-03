package org.walkgis.learngis.lesson7.basicclasses;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.awt.*;
import java.util.ArrayList;

public class GISAttribute {
    public ArrayList values = new ArrayList<>();

    public GISAttribute(Object... attrs) {
        for (Object str : attrs) {
            values.add(str);
        }
    }

    public void addValue(Object o) {
        values.add(o);
    }

    public Object getValue(int index) {
        return values.get(index);
    }

    public void draw(GraphicsContext graphicsContext, GISView gisView, GISVertex location, int index) {
        Point screenPoint = gisView.toScreenPoint(location);
        graphicsContext.setFont(new Font("宋体", 20));
        graphicsContext.setFill(Color.BLUE);

        graphicsContext.fillText(getValue(index).toString(), screenPoint.x, screenPoint.y);
    }
}
