package org.walkgis.learngis.lesson20.basicclasses.network;

import org.walkgis.learngis.lesson20.basicclasses.*;
import java.util.ArrayList;
import java.util.List;

public class GISNetwork {
    //结点列表
    public List<GISNode> nodes = new ArrayList<>();
    //弧段列表
    public List<GISArc> arcs = new ArrayList<>();
    //邻接矩阵
    public GISArc[][] matrix;
    //原始线图层
    public GISVectorLayer lineLayer;

    public GISNetwork(GISVectorLayer lineLayer, Integer fieldIndex, Double tolerance) {
        if (fieldIndex == null) fieldIndex = -1;
        if (tolerance == null) tolerance = -1.0;

        this.lineLayer = lineLayer;
        //如果该图层不是线图层，则返回
        if (lineLayer.shapeType != SHAPETYPE.polyline) return;
        //如果用户没有提供tolerance，计算
        if (tolerance < 0) {
            tolerance = Double.MAX_VALUE;
            for (int i = 0; i < lineLayer.featureCount(); i++) {
                GISPolyline line = (GISPolyline) lineLayer.features.get(i).spatial;
                tolerance = Math.min(tolerance, line.length);
            }
            //找出最小的线实体长度，令其缩小100倍，作为tolerance
            tolerance /= 100;
        }

        //填充结点列表和弧段列表
        for (int i = 0; i < lineLayer.featureCount(); i++) {
            GISPolyline line = (GISPolyline) lineLayer.features.get(i).spatial;
            //获取对应节点
            int from = findOrInsertNode(line.fromNode(), tolerance);
            int to = findOrInsertNode(line.toNode(), tolerance);
            //获得阻抗，可以是自己设置的一个属性或者是弧段长度
            double impedence = (fieldIndex > 0) ? (double) (lineLayer.features.get(i).getAttribute(fieldIndex)) : line.length;
            //增加到弧段列表
            arcs.add(new GISArc(lineLayer.features.get(i), from, to, impedence));
        }
        //建立邻接矩阵
        buildMatrix();
    }

    public GISVectorLayer createNodeLayer() {
        GISVectorLayer nodeLayer = new GISVectorLayer("nodes", SHAPETYPE.point, lineLayer.extent);
        for (int i = 0; i < nodes.size(); i++)
            nodeLayer.addFeature(new GISFeature(new GISPoint(nodes.get(i).location), new GISAttribute()));
        return nodeLayer;
    }

    public GISVectorLayer createArcLayer() {
        //生成属性字段
        List<GISField> fields = new ArrayList<>();
        fields.add(new GISField(Integer.class, "fromNodeIndex"));
        fields.add(new GISField(Integer.class, "toNodeIndex"));
        fields.add(new GISField(Double.class, "impedence"));
        //生成图层
        GISVectorLayer arcLayer = new GISVectorLayer("arcs", SHAPETYPE.polyline, lineLayer.extent, fields);
        for (int i = 0; i < arcs.size(); i++) {
            GISAttribute a = new GISAttribute();
            a.addValue(arcs.get(i).fromNodeIndex);
            a.addValue(arcs.get(i).toNodeIndex);
            a.addValue(arcs.get(i).impedence);
            //添加控件对象
            arcLayer.addFeature(new GISFeature(arcs.get(i).feature.spatial, a));
        }
        return arcLayer;
    }

    public void readNodeLayer(GISVectorLayer nodeLayer) {
        nodes.clear();
        for (int i = 0; i < nodeLayer.features.size(); i++) {
            nodes.add(new GISNode(nodeLayer.features.get(i).spatial.center));
        }
    }

    public void readArcLayer(GISVectorLayer arcLayer) {
        arcs.clear();
        for (int i = 0; i < arcLayer.features.size(); i++) {
            GISFeature gf = arcLayer.features.get(i);
            int from = (int) gf.getAttribute(0);
            int to = (int) gf.getAttribute(1);
            double impedence = (double) gf.getAttribute(2);
            arcs.add(new GISArc(lineLayer.features.get(i), from, to, impedence));
        }
    }

    private void buildMatrix() {
        //初始化邻接矩阵
        matrix = new GISArc[nodes.size()][nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                matrix[i][j] = null;
            }
        }

        //填充邻接矩阵，假定每个弧段都为双向通行，且阻抗相同
        for (int i = 0; i < arcs.size(); i++) {
            matrix[arcs.get(i).fromNodeIndex][arcs.get(i).toNodeIndex] = arcs.get(i);
        }
    }

    private int findOrInsertNode(GISVertex vertex, Double tolerance) {
        //在nodes中查看该位置是否已经存在一个节点，如果是就直接返回这个节点
        for (int i = 0; i < nodes.size(); i++)
            if (nodes.get(i).location.distance(vertex) < tolerance) return i;
        //该位置没有节点，则新增一个节点
        nodes.add(new GISNode(vertex));
        return nodes.size() - 1;
    }

    public List<GISFeature> findRoute(int fromNodeIndex, int toNodeIndex) {
        //初始化路径记录
        List<GISFeature> route = new ArrayList<>();
        //起点终点相同，所以直接返回空路径
        if (fromNodeIndex == toNodeIndex) return route;
        //定义并初始化相关变量
        double[] dist = new double[nodes.size()];
        int[] prov = new int[nodes.size()];
        List<Integer> q = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            dist[i] = Double.MAX_VALUE;
            prov[i] = -1;
            q.add(i);
        }
        dist[fromNodeIndex] = 0;
        boolean findPath = false;
        while (q.size() > 0) {
            //寻找Q中dist值最小的节点
            int minIndex = 0;
            for (int i = 1; i < q.size(); i++) {
                if (dist[q.get(i)] < dist[q.get(minIndex)]) minIndex = i;
            }
            //如果节点是终点，则退出循环
            if (q.get(minIndex) == toNodeIndex) {
                findPath = false;
                break;
            }
            //更新dist及prov
            for (int i = 0; i < q.size(); i++) {
                if (minIndex == i) continue;
                if (matrix[q.get(minIndex)][q.get(i)] == null) continue;

                double newDist = dist[q.get(minIndex)] + matrix[q.get(minIndex)][q.get(i)].impedence;
                if (newDist < dist[q.get(i)]) {
                    dist[q.get(i)] = newDist;
                    prov[q.get(i)] = q.get(minIndex);
                }
            }
            //移除已经确定最短距离的结点
            q.remove(minIndex);
        }
        //如果有路径存在，通过倒序的方法找到沿路的弧段
        if (findPath) {
            int i = toNodeIndex;
            while (prov[i] > -1) {
                route.add(0, matrix[prov[i]][i].feature);
                i = prov[i];
            }
        }
        return route;
    }

    /**
     * 根据位置找到最近的结点序号
     *
     * @param vertex
     * @return
     */
    public int findNearestNodeIndex(GISVertex vertex) {
        double minDist = Double.MAX_VALUE;
        int minIndex = -1;
        for (int i = 0; i < nodes.size(); i++) {
            double dist = nodes.get(i).location.distance(vertex);
            if (dist < minDist) {
                minIndex = i;
                minDist = dist;
            }
        }
        return minIndex;
    }

    /**
     * 根据起止点位置计算最短路径
     *
     * @param vFrom
     * @param vTo
     * @return
     */
    public List<GISFeature> findRoute(GISVertex vFrom, GISVertex vTo) {
        int fromNodeIndex = findNearestNodeIndex(vFrom);
        int toNodeIndex = findNearestNodeIndex(vTo);
        return findRoute(fromNodeIndex, toNodeIndex);
    }
}
