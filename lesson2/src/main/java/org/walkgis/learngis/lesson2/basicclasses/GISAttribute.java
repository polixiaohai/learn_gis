package org.walkgis.learngis.lesson2.basicclasses;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;

public class GISAttribute {
    public ArrayList values = new ArrayList<>();

    public void addValue(Object o) {
        values.add(o);
    }

    public Object getValue(int index) {
        return values.get(index);
    }

    public void draw(GraphicsContext graphicsContext, GISVertex location, int index) {
        graphicsContext.setFont(new Font("宋体", 20));
        graphicsContext.setFill(Color.BLUE);

        graphicsContext.fillText(getValue(index).toString(), (int) location.x, (int) location.y);
    }
}
