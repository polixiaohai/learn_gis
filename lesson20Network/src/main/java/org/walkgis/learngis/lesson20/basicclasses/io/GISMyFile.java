package org.walkgis.learngis.lesson20.basicclasses.io;

import org.walkgis.learngis.lesson20.basicclasses.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class GISMyFile {
    public static class MyFileHeader {
        public double minx;
        public double miny;
        public double maxx;
        public double maxy;
        public int shapeType;
        public int fieldCount;
        public int featureCount;
    }

    public void writeSingleFile(GISVectorLayer layer, String filename) {
        RandomAccessFile bw = null;
        try {
            bw = new RandomAccessFile(filename, "rw");
            //写文件头
            writeFileHeader(layer, bw);
            //写图层名称
            GISTools.writeString(layer.name, bw);
            //写属性字段结构
            for (GISField field : layer.fields) {
                GISTools.writeString(field.dataType.toString(), bw);
                GISTools.writeString(field.fieldName, bw);
            }
            //写空间对象类型
            for (GISFeature f : layer.features)
                writeFeature(f, bw);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //其它内容
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public GISLayer readSingleFile(String filename) {
        RandomAccessFile br = null;
        GISVectorLayer layer = null;
        try {
            br = new RandomAccessFile(filename, "rw");
            //读文件头
            MyFileHeader mfh = (MyFileHeader) GISTools.fromBytes(br, MyFileHeader.class);
            //读图层名称
            String name = GISTools.readString(br);
            //读属性字段结构
            List<GISField> fields = readFields(mfh.fieldCount, br);
            //定义图层
            SHAPETYPE ShapeType = Enum.valueOf(SHAPETYPE.class, String.valueOf(mfh.shapeType));
            GISExtent extent = new GISExtent(new GISVertex(mfh.minx, mfh.miny), new GISVertex(mfh.maxx, mfh.maxy));
            layer = new GISVectorLayer(name, ShapeType, extent, fields);
            //读空间对象类型
            for (int i = 0; i < mfh.featureCount; i++) {
                GISSpatial spatial = readSpatial(ShapeType, br);
                GISAttribute attribute = readAttribute(br, fields);
                layer.addFeature(new GISFeature(spatial, attribute),false);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //关闭文件并返回结果
            try {
                if (br != null)
                    br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return layer;
    }

    public static void writeFileHeader(GISVectorLayer layer, RandomAccessFile bw) throws IOException {
        MyFileHeader mfh = new MyFileHeader();
        mfh.minx = layer.extent.getMinX();
        mfh.miny = layer.extent.getMinY();
        mfh.maxx = layer.extent.getMaxX();
        mfh.maxy = layer.extent.getMaxY();
        mfh.featureCount = layer.features.size();
        mfh.shapeType = layer.shapeType.getValue();
        mfh.fieldCount = layer.fields.size();
        bw.write(GISTools.toBytes(mfh));
    }

    private static List<GISVertex> readMultipleVertexes(RandomAccessFile br) throws IOException {
        List<GISVertex> vs = new ArrayList<>();
        int vcount = br.readInt();
        for (int vc = 0; vc < vcount; vc++)
            vs.add(new GISVertex(br));
        return vs;
    }

    private static GISAttribute readAttribute(RandomAccessFile br, List<GISField> fields) throws IOException, ClassNotFoundException {
        GISAttribute a = new GISAttribute();
        int count = br.readInt();
        for (int vc = 0; vc < count; vc++)
            a.addValue(fields.get(vc).myFileValueToObject(GISTools.readString(br)));
        return a;
    }

    private static GISSpatial readSpatial(SHAPETYPE shapeType, RandomAccessFile br) throws IOException {
        if (shapeType == SHAPETYPE.point)
            return new GISPoint(new GISVertex(br));
        if (shapeType == SHAPETYPE.polyline)
            return new GISPolyline(readMultipleVertexes(br));
        if (shapeType == SHAPETYPE.polygon)
            return new GISPolygon(readMultipleVertexes(br));
        return null;
    }


    ////////////////////////////////////////////////
    public static void writeFile(GISVectorLayer layer, String fileName) {
        List<GISVectorLayer> layers = new ArrayList<>();
        layers.add(layer);
        writeFileMultiLayers(layers, fileName);
    }

    public static void writeFileMultiLayers(List<GISVectorLayer> layers, String fileName) {
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(fileName, "rw");
            for (GISVectorLayer layer : layers) {
                writeFileHeader(layer, randomAccessFile);
                GISTools.writeString(layer.name, randomAccessFile);
                writeFields(layer.fields, randomAccessFile);
                writeFeatures(layer.features, randomAccessFile);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (randomAccessFile != null) randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void writeFeatures(List<GISFeature> features, RandomAccessFile randomAccessFile) throws IOException {
        for (GISFeature feature : features)
            writeFeature(feature, randomAccessFile);
    }

    public static void writeFeature(GISFeature feature, RandomAccessFile bw) throws IOException {
        if (feature.spatial instanceof GISPoint) {
            ((GISPoint) feature.spatial).center.writeVertex(bw);
        }
        if (feature.spatial instanceof GISPolyline) {
            writeMultipleVertexes(((GISPolyline) feature.spatial).vertices, bw);
        }
        if (feature.spatial instanceof GISPolygon) {
            writeMultipleVertexes(((GISPolygon) feature.spatial).vertexs, bw);
        }
        writeAttribute(feature.attribute, bw);
    }

    private static void writeAttribute(GISAttribute attribute, RandomAccessFile bw) throws IOException {
        bw.write(attribute.values.size());
        for (Object value : attribute.values)
            GISTools.writeString(value.toString(), bw);
    }

    private static void writeMultipleVertexes(List<GISVertex> vertices, RandomAccessFile bw) throws IOException {
        bw.write(vertices.size());
        for (int vc = 0; vc < vertices.size(); vc++)
            vertices.get(vc).writeVertex(bw);
    }

    private static void writeFields(List<GISField> fields, RandomAccessFile randomAccessFile) {
        for (GISField field : fields) {
            GISTools.writeString(field.dataType.getName(), randomAccessFile);
            GISTools.writeString(field.fieldName, randomAccessFile);
        }
    }


    ////////////////////////////////////////////////
    public static GISVectorLayer readFile(String fileName) {
        List<GISVectorLayer> layers = readFileMultiLayers(fileName);
        return layers.get(0);
    }

    public static List<GISVectorLayer> readFileMultiLayers(String fileName) {
        RandomAccessFile randomAccessFile = null;
        List<GISVectorLayer> layers = new ArrayList<>();
        try {
            randomAccessFile = new RandomAccessFile(fileName, "r");
            while (randomAccessFile.read() != -1) {
                MyFileHeader mfh = (MyFileHeader) GISTools.fromBytes(randomAccessFile, MyFileHeader.class);
                SHAPETYPE shapetype = Enum.valueOf(SHAPETYPE.class, String.valueOf(mfh.shapeType));
                GISExtent extent = new GISExtent(mfh.minx, mfh.maxx, mfh.miny, mfh.maxy);
                String layerName = GISTools.readString(randomAccessFile);
                List<GISField> fields = readFields(mfh.fieldCount, randomAccessFile);
                GISVectorLayer layer = new GISVectorLayer(layerName, shapetype, extent, fields);
                readFeatures(layer, randomAccessFile, mfh.featureCount);
                layers.add(layer);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (randomAccessFile != null) randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return layers;
    }

    private static List<GISField> readFields(int fieldCount, RandomAccessFile br) throws ClassNotFoundException {
        List<GISField> fields = new ArrayList<>();
        for (int fieldindex = 0; fieldindex < fieldCount; fieldindex++) {
            fields.add(new GISField(br));
        }
        return fields;
    }

    private static void readFeatures(GISVectorLayer layer, RandomAccessFile randomAccessFile, int featureCount) throws IOException, ClassNotFoundException {
        for (int i = 0; i < featureCount; i++) {
            GISSpatial spatial = readSpatial(layer.shapeType, randomAccessFile);
            GISAttribute attribute = readAttribute(randomAccessFile, layer.fields);
            layer.addFeature(new GISFeature(spatial, attribute),false);
        }
    }
}
