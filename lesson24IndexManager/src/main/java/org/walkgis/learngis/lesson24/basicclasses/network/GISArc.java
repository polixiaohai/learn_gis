package org.walkgis.learngis.lesson24.basicclasses.network;

import org.walkgis.learngis.lesson24.basicclasses.GISFeature;

public class GISArc {
    public GISFeature feature;
    public int fromNodeIndex;
    public int toNodeIndex;
    //阻抗
    public double impedence;

    public GISArc(GISFeature feature, int fromNodeIndex, int toNodeIndex, double impedence) {
        this.feature = feature;
        this.fromNodeIndex = fromNodeIndex;
        this.toNodeIndex = toNodeIndex;
        this.impedence = impedence;
    }
}
