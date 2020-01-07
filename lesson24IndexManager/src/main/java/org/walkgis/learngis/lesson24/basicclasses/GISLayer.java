package org.walkgis.learngis.lesson24.basicclasses;

import org.walkgis.learngis.lesson24.basicclasses.enums.LAYERTYPE;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public abstract class GISLayer {
    public String name;
    public GISExtent extent;
    public boolean visible = true;
    public String path = "";
    public LAYERTYPE layerType;

    public abstract void draw(Graphics2D graphics2D, GISView view, GISExtent extent);

    public abstract void write(DataOutputStream dataOutputStream);

    public abstract void read(DataInputStream dataInputStream);
}
