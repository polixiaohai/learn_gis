package org.walkgis.learngis.lesson24.basicclasses.index;


import org.walkgis.learngis.lesson24.basicclasses.*;
import org.walkgis.learngis.lesson24.basicclasses.enums.SHAPETYPE;
import org.walkgis.learngis.lesson24.basicclasses.geometry.GISPolyline;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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

    public GISVectorLayer getTreeLayer() {
        List<NodeEntry> nodes = new ArrayList<>();
        nodeList(nodes, root);
        List<GISField> fields = new ArrayList<>();
        fields.add(new GISField(Integer.class, "Level"));
        GISVectorLayer treeLayer = new GISVectorLayer("treeLayer", SHAPETYPE.polyline, null, fields);
        for (int i = 0; i < nodes.size(); i++) {
            List<GISVertex> vs = new ArrayList<>();
            vs.add(new GISVertex(nodes.get(i).mbr.getMaxX(), nodes.get(i).mbr.getMaxY()));
            vs.add(new GISVertex(nodes.get(i).mbr.getMaxX(), nodes.get(i).mbr.getMinY()));
            vs.add(new GISVertex(nodes.get(i).mbr.getMinX(), nodes.get(i).mbr.getMinY()));
            vs.add(new GISVertex(nodes.get(i).mbr.getMinX(), nodes.get(i).mbr.getMaxY()));
            vs.add(new GISVertex(nodes.get(i).mbr.getMaxX(), nodes.get(i).mbr.getMaxY()));
            GISPolyline line = new GISPolyline(vs);
            GISAttribute a = new GISAttribute();
            a.addValue(nodes.get(i).level);
            treeLayer.addFeature(new GISFeature(line, a), true);
        }
        return treeLayer;
    }

    /**
     * 树检索
     *
     * @param extent
     * @param onlyInclude
     * @return
     */
    public List<GISFeature> query(GISExtent extent, boolean onlyInclude) {
        List<GISFeature> features = new ArrayList<>();
        findFeatures(root, features, extent, onlyInclude);
        return features;
    }

    /**
     * 写文件
     *
     * @param dataOutputStream
     */
    public void writeFile(DataOutputStream dataOutputStream) {
        if (root.entries.size() == 0) return;
        writeNode(root, dataOutputStream);
    }

    /**
     * 读文件
     *
     * @param br
     */
    public void readFile(DataInputStream br) {
        if (layer.features.size() == 0) return;
        try {
            root = readNode(br);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /////////////////////////////////////////私有方法/////////////////////////////////////////

    private void findFeatures(NodeEntry node, List<GISFeature> features, GISExtent extent, boolean onlyInclude) {
        //如果是叶子结点
        if (node.level == 1) {
            for (NodeEntry entry : node.entries) {
                //如果数据的mbr与搜索范围不相交，则忽略
                if (!entry.mbr.insertectOrNot(extent)) continue;
                //如果不需要搜索范围覆盖mbr则入选返回列表
                if (!onlyInclude) features.add(entry.feature);
                    //搜索范围需要覆盖数据的mbr，且确实覆盖了，则入选返回列表
                else if (extent.include(entry.mbr)) features.add(entry.feature);
            }
        } else {//如果是非叶子结点
            for (NodeEntry entry : node.entries) {
                //如果子结点的mbr与搜索范围相交，则搜索该子结点
                if (entry.mbr.insertectOrNot(extent))
                    findFeatures(entry, features, extent, onlyInclude);
            }
        }
    }

    private NodeEntry readNode(DataInputStream br) throws IOException {
        int level = br.readInt();
        if (level == 0) {//数据结点
            int index = br.readInt();
            NodeEntry node = new NodeEntry(layer.features.get(index), index);
            return node;
        } else {//树结点
            NodeEntry node = new NodeEntry(level);
            node.mbr = new GISExtent(br);
            int entryCount = br.readInt();
            for (int i = 0; i < entryCount; i++) {
                NodeEntry childNode = readNode(br);
                //恢复父子关系
                childNode.parent = node;
                node.entries.add(childNode);
            }
            return node;
        }
    }

    private void writeNode(NodeEntry node, DataOutputStream dataOutputStream) {
        try {
            dataOutputStream.writeInt(node.level);
            if (node.level == 0)//数据结点
                dataOutputStream.writeInt(node.featureIndex);
            else {//树结点
                node.mbr.write(dataOutputStream);
                dataOutputStream.writeInt(node.entries.size());
                for (int i = 0; i < node.entries.size(); i++)
                    writeNode(node.entries.get(i), dataOutputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void nodeList(List<NodeEntry> nodes, NodeEntry node) {
        nodes.add(node);
        if (node.entries == null) return;
        for (int i = 0; i < node.entries.size(); i++)
            nodeList(nodes, node.entries.get(i));
    }

    private void adjustTree(NodeEntry oneNode, NodeEntry splitNode) {
        //oneNode是根节点
        if (oneNode.parent == null) {
            //出现一个兄弟，则需要向上生长
            if (splitNode != null) {
                //新生长的根结点，肯定不是叶子结点
                NodeEntry newRoot = new NodeEntry(oneNode.level + 1);
                newRoot.addEntry(oneNode);
                newRoot.addEntry(splitNode);
                root = newRoot;
            }
            return;
        }
        //找到原有结点的夫结点
        NodeEntry parent = oneNode.parent;
        //调整父结点的mbr
        parent.mbr.merge(oneNode.mbr);
        //将被分割出来的结点插入父结点的入口列表
        insertNode(parent, splitNode);
    }

    private NodeEntry splitNode(NodeEntry oneNode) {
        //找到两个种子的entities序号，seed2》seed1
        int seed1 = 0, seed2 = 1;
        //寻找可以最大化未重叠面积的，即两个种子间隔最远
        double maxArea = Double.NEGATIVE_INFINITY;
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
        maxDiffArea = Double.NEGATIVE_INFINITY;
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
        double minEnlargement = Double.POSITIVE_INFINITY;
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
