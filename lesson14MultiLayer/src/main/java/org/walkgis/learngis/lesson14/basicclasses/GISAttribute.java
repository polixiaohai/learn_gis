package org.walkgis.learngis.lesson14.basicclasses;

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

    public void draw(Graphics graphicsContext, GISView gisView, GISVertex location, int index) {
        Point screenPoint = gisView.toScreenPoint(location);
        graphicsContext.setFont(new Font("宋体", 0, 9));
        graphicsContext.setColor(Color.BLUE);

        graphicsContext.drawString(getValue(index).toString(), screenPoint.x, screenPoint.y);
    }
}
