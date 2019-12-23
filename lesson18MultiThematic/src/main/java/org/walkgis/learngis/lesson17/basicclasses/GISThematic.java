package org.walkgis.learngis.lesson17.basicclasses;

import java.awt.*;

public class GISThematic {
    public Color outsideColor;
    public  int size;
    public  Color insideColor;

    public GISThematic(Color outsideColor, int size, Color insideColor) {
        update(outsideColor, size, insideColor);
    }

    public GISThematic(SHAPETYPE shapeType) {
        if (shapeType == SHAPETYPE.point)
            update(GISTools.getRandomColor(), GISConst.pointSize, GISTools.getRandomColor());
        else if(shapeType==SHAPETYPE.polyline)
            update(GISTools.getRandomColor(), GISConst.lineWidth, GISTools.getRandomColor());
        else if (shapeType==SHAPETYPE.polygon)
            update(GISTools.getRandomColor(), GISConst.polygonBoundaryWidth, GISTools.getRandomColor());
    }

    public void update(Color outsideColor, int size, Color insideColor) {
        this.outsideColor = outsideColor;
        this.size = size;
        this.insideColor = insideColor;
    }
}
