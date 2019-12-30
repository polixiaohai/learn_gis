package org.walkgis.learngis.lesson23.basicclasses.index;

import org.walkgis.learngis.lesson23.basicclasses.GISExtent;
import org.walkgis.learngis.lesson23.basicclasses.GISFeature;
import java.util.ArrayList;
import java.util.List;

public class NodeEntry {
    public GISExtent mbr = null;
    public int featureIndex;
    public GISFeature feature = null;
    public List<NodeEntry> entries = null;
    public NodeEntry parent = null;
    public int level;

    /**
     * 专门用于树节点的生成
     *
     * @param level
     */
    public NodeEntry(int level) {
        this.level = level;
        this.entries = new ArrayList<>();
    }

    /**
     * 专门用于数据节点的生成
     *
     * @param feature
     * @param featureIndex
     */
    public NodeEntry(GISFeature feature, int featureIndex) {
        this.featureIndex = featureIndex;
        this.feature = feature;
        this.mbr = feature.spatial.extent;
        this.level = 0;
    }

    public void addEntry(NodeEntry node) {
        //如果子节点为空，就返回
        if (node==null)return;
        this.entries.add(node);
        if (mbr==null) mbr = new GISExtent(node.mbr);
        else mbr.merge(node.mbr);
        //指定子节点的父节点
        node.parent = this;
    }
}
