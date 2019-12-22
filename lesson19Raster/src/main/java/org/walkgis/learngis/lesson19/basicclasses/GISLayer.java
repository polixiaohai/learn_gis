package org.walkgis.learngis.lesson19.basicclasses;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class GISLayer {
    public String name;
    public GISExtent extent;
    public boolean visible = true;
    public String path = "";
    public LAYERTYPE layerType;

    public abstract void draw(Graphics2D graphics2D, GISView view, GISExtent extent);
}
