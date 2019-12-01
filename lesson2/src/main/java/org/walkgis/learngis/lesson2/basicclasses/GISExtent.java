package org.walkgis.learngis.lesson4.basicclasses;

public class GISExtent {
    public GISVertex bottomLeft;
    public GISVertex upRight;

    public GISExtent(GISVertex bottomLeft, GISVertex upRight) {
        this.bottomLeft = bottomLeft;
        this.upRight = upRight;
    }
}
