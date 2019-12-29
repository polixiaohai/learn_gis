package org.walkgis.learngis.lesson23.basicclasses.index;


import javafx.beans.property.DoubleProperty;
import org.walkgis.learngis.lesson23.basicclasses.GISExtent;
import org.walkgis.learngis.lesson23.basicclasses.GISFeature;
import org.walkgis.learngis.lesson23.basicclasses.GISVectorLayer;

import javax.xml.soap.Node;
import java.util.ArrayList;
import java.util.List;

public class RTree {
    //根节点
    public NodeEntry root;
    //每个结点上最大人口数
    public int maxEntries;
    //每个节点上最小人口数
    public int minEntries;
    //与此树关联的图层
    public GISVectorLayer layer;

    public RTree(GISVectorLayer layer, int maxEntries) {
        this.root = new NodeEntry(1);
        this.maxEntries = Math.max(maxEntries, 2);
        this.minEntries = maxEntries / 2;
        this.layer = layer;
    }

    public RTree(GISVectorLayer layer) {
        this(layer, 4);
    }

    /**
     * 仅仅用于插入数据
     *
     * @param index
     */
    public void insertData(int index) {
        GISFeature feature = layer.features.get(index);
        //生成数据结点
        NodeEntry dataEntry = new NodeEntry(feature, index);
        //从树根开始，找到一个叶子结点
        NodeEntry leafNode = chooseLeaf(root, dataEntry);
        //把数据入口插入叶子结点
        insertNode(leafNode, dataEntry);
    }

    /**
     * 将子树结点插入到一个父结点的入口列表中
     *
     * @param parentNode
     * @param childNode
     */
    public void insertNode(NodeEntry parentNode, NodeEntry childNode) {
        parentNode.addEntry(childNode);
        //如果父节点的入口数量超限，则需要分割出一个叔叔结点
        NodeEntry uncleNode = (parentNode.entries.size() > maxEntries) ? splitNode(parentNode) : null;
        //调整上层树结构
        adjustTree(parentNode, uncleNode);
    }

    private void adjustTree(NodeEntry oneNode, NodeEntry splitNode) {
        if (oneNode.parent == null) {
            if (splitNode != null) {
                NodeEntry newRoot = new NodeEntry(oneNode.level + 1);
                newRoot.addEntry(oneNode);
                newRoot.addEntry(splitNode);
                root = newRoot;
            }
            return;
        }
        NodeEntry parent = oneNode.parent;
        parent.mbr.merge(oneNode.mbr);
        insertNode(parent, splitNode);
    }

    private NodeEntry splitNode(NodeEntry oneNode) {
        //找到两个种子的entities序号，seed2》seed1
        int seed1 = 0, seed2 = 1;
        //寻找可以最大化未重叠面积的，即两个种子间隔最远
        double maxArea = Double.MIN_VALUE;
        for (int i = 0; i < oneNode.entries.size() - 1; i++)
            for (int j = i + 1; j < oneNode.entries.size(); j++) {
                //计算未覆盖面积
                double area = new GISExtent(oneNode.entries.get(i).mbr, oneNode.entries.get(j).mbr).area -
                        oneNode.entries.get(i).mbr.area - oneNode.entries.get(j).mbr.area;
                if (area > maxArea) {
                    seed1 = i;
                    seed2 = j;
                    maxArea = area;
                }
            }
        //待分割所有入口，包括两个种子入口
        List<NodeEntry> leftEntries = oneNode.entries;
        //生成原有结点的兄弟结点，两个结点level相同
        NodeEntry splitNode = new NodeEntry(oneNode.level);
        //给分割结点一个种子
        splitNode.addEntry(leftEntries.get(seed2));
        //情况原有结点的入口
        oneNode.entries = new ArrayList<>();
        oneNode.mbr = null;
        oneNode.addEntry(leftEntries.get(seed1));
        leftEntries.remove(seed2);
        leftEntries.remove(seed1);

        while (leftEntries.size() > 0) {
            if (oneNode.entries.size() + leftEntries.size() == minEntries) {
                assignAllEntries(oneNode, leftEntries);
                break;
            } else if (splitNode.entries.size() + leftEntries.size() == minEntries) {
                assignAllEntries(splitNode, leftEntries);
                break;
            }
            Double diffArea = 0.0;
            int index = pickNext(oneNode, splitNode, leftEntries, diffArea);
            if (diffArea < 0) oneNode.addEntry(leftEntries.get(index));
            else if (diffArea > 0) splitNode.addEntry(leftEntries.get(index));
            else {
                double merge1 = new GISExtent(leftEntries.get(index).mbr, oneNode.mbr).area;
                double merge2 = new GISExtent(leftEntries.get(index).mbr, splitNode.mbr).area;
                if (merge1 < merge2) oneNode.addEntry(leftEntries.get(index));
                else if (merge1 > merge2) splitNode.addEntry(leftEntries.get(index));
                else {
                    if (oneNode.entries.size() < splitNode.entries.size()) oneNode.addEntry(leftEntries.get(index));
                    else splitNode.addEntry(leftEntries.get(index));
                }
            }
            leftEntries.remove(index);
            if (oneNode.entries.size() + leftEntries.size() == minEntries)
                assignAllEntries(oneNode, leftEntries);
            else if (splitNode.entries.size() + leftEntries.size() == minEntries)
                assignAllEntries(splitNode, leftEntries);
        }
        return splitNode;
    }

    private void assignAllEntries(NodeEntry node, List<NodeEntry> entries) {
        for (int i = 0; i < entries.size(); i++)
            node.addEntry(entries.get(i));
        entries.clear();
    }

    private int pickNext(NodeEntry firstNode, NodeEntry secondNode, List<NodeEntry> entries, Double maxDiffArea) {
        maxDiffArea = Double.MAX_VALUE;
        int index = -1;
        for (int i = 0; i < entries.size(); i++) {
            double diffArea = enlargedArea(firstNode, entries.get(i)) - enlargedArea(secondNode, entries.get(i));
            if (Math.abs(diffArea) > maxDiffArea) {
                maxDiffArea = Math.abs(diffArea);
                index = i;
            }
        }
        maxDiffArea = enlargedArea(firstNode, entries.get(index)) - enlargedArea(secondNode, entries.get(index));
        return index;
    }

    private NodeEntry chooseLeaf(NodeEntry node, NodeEntry entry) {
        //如果到达叶子结点，就返回
        if (node.level == 1) return node;
        //寻找扩大面积最小的子结点序号
        double minEnlargement = Double.MAX_VALUE;
        int minIndex = -1;
        for (int i = 0; i < node.entries.size(); i++) {
            double enlargement = enlargedArea(node.entries.get(i), entry);
            if (enlargement < minEnlargement) {
                minIndex = i;
                minEnlargement = enlargement;
            }
        }
        //递归方法，继续调用查找下一级结点
        return chooseLeaf(node.entries.get(minIndex), entry);
    }

    private double enlargedArea(NodeEntry node, NodeEntry entry) {
        return new GISExtent(entry.mbr, node.mbr).area - node.mbr.area;
    }
}
