package org.walkgis.learngis.lesson23.basicclasses.io;

import org.walkgis.learngis.lesson23.basicclasses.*;

import java.io.*;
import java.nio.file.*;
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

        //写入数据
        public void write(DataOutputStream out) throws IOException {
            out.writeDouble(this.minx);
            out.writeDouble(this.miny);
            out.writeDouble(this.maxx);
            out.writeDouble(this.maxy);
            out.writeInt(this.shapeType);
            out.writeInt(this.fieldCount);
            out.writeInt(this.featureCount);
        }

        //读取数据
        public void read(DataInputStream input) throws IOException {
            this.minx = input.readDouble();
            this.miny = input.readDouble();
            this.maxx = input.readDouble();
            this.maxy = input.readDouble();
            this.shapeType = input.readInt();
            this.fieldCount = input.readInt();
            this.featureCount = input.readInt();
        }
    }

    public void writeSingleFile(GISVectorLayer layer, String filename) {
        BufferedOutputStream bufferOut = null;
        DataOutputStream dataOut = null;
        try {
            Path path = Paths.get(filename);
            bufferOut = new BufferedOutputStream(Files.newOutputStream(path, StandardOpenOption.WRITE));
            dataOut = new DataOutputStream(bufferOut);
            //写文件头
            writeFileHeader(layer, dataOut);
            //写图层名称
            GISTools.writeString(layer.name, dataOut);
            //写属性字段结构
            for (GISField field : layer.fields) {
                GISTools.writeString(field.dataType.toString(), dataOut);
                GISTools.writeString(field.fieldName, dataOut);
            }
            //写空间对象类型
            for (GISFeature f : layer.features)
                writeFeature(f, dataOut);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //其它内容
            try {
                dataOut.close();
                bufferOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public GISLayer readSingleFile(String filename) {
        BufferedInputStream bufferInput = null;
        DataInputStream dataInput = null;
        GISVectorLayer layer = null;
        try {
            bufferInput = new BufferedInputStream(new FileInputStream(filename));
            dataInput = new DataInputStream(bufferInput);
            //读文件头
            MyFileHeader mfh = new MyFileHeader();
            mfh.read(dataInput);
            //读图层名称
            String name = GISTools.readString(dataInput);
            //读属性字段结构
            List<GISField> fields = readFields(mfh.fieldCount, dataInput);
            //定义图层
            SHAPETYPE ShapeType = Enum.valueOf(SHAPETYPE.class, String.valueOf(mfh.shapeType));
            GISExtent extent = new GISExtent(new GISVertex(mfh.minx, mfh.miny), new GISVertex(mfh.maxx, mfh.maxy));
            layer = new GISVectorLayer(name, ShapeType, extent, fields);
            //读空间对象类型
            for (int i = 0; i < mfh.featureCount; i++) {
                GISSpatial spatial = readSpatial(ShapeType, dataInput);
                GISAttribute attribute = readAttribute(dataInput, fields);
                layer.addFeature(new GISFeature(spatial, attribute), false);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                dataInput.close();
                bufferInput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return layer;
    }

    public static void writeFileHeader(GISVectorLayer layer, DataOutputStream bw) throws IOException {
        MyFileHeader mfh = new MyFileHeader();
        mfh.minx = layer.extent.getMinX();
        mfh.miny = layer.extent.getMinY();
        mfh.maxx = layer.extent.getMaxX();
        mfh.maxy = layer.extent.getMaxY();
        mfh.featureCount = layer.features.size();
        mfh.shapeType = layer.shapeType.getValue();
        mfh.fieldCount = layer.fields.size();
        mfh.write(bw);
    }

    private static List<GISVertex> readMultipleVertexes(DataInputStream br) throws IOException {
        List<GISVertex> vs = new ArrayList<>();
        int vcount = br.readInt();
        for (int vc = 0; vc < vcount; vc++)
            vs.add(new GISVertex(br));
        return vs;
    }

    private static GISAttribute readAttribute(DataInputStream br, List<GISField> fields) throws IOException, ClassNotFoundException {
        GISAttribute a = new GISAttribute();
        int count = br.readInt();
        for (int vc = 0; vc < count; vc++)
            a.addValue(fields.get(vc).myFileValueToObject(GISTools.readString(br)));
        return a;
    }

    private static GISSpatial readSpatial(SHAPETYPE shapeType, DataInputStream br) throws IOException {
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
        BufferedOutputStream bufferOut = null;
        DataOutputStream dataOut = null;
        try {
            Path path = Paths.get(fileName);
            bufferOut = new BufferedOutputStream(Files.newOutputStream(path, StandardOpenOption.WRITE));
            dataOut = new DataOutputStream(bufferOut);
            for (GISVectorLayer layer : layers) {
                writeFileHeader(layer, dataOut);
                GISTools.writeString(layer.name, dataOut);
                writeFields(layer.fields, dataOut);
                writeFeatures(layer.features, dataOut);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                dataOut.close();
                bufferOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void writeFeatures(List<GISFeature> features, DataOutputStream randomAccessFile) throws IOException {
        for (GISFeature feature : features)
            writeFeature(feature, randomAccessFile);
    }

    public static void writeFeature(GISFeature feature, DataOutputStream bw) throws IOException {
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

    private static void writeAttribute(GISAttribute attribute, DataOutputStream bw) throws IOException {
        bw.write(attribute.values.size());
        for (Object value : attribute.values)
            GISTools.writeString(value.toString(), bw);
    }

    private static void writeMultipleVertexes(List<GISVertex> vertices, DataOutputStream bw) throws IOException {
        bw.write(vertices.size());
        for (int vc = 0; vc < vertices.size(); vc++)
            vertices.get(vc).writeVertex(bw);
    }

    private static void writeFields(List<GISField> fields, DataOutputStream randomAccessFile) {
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
        BufferedInputStream bufferInput = null;
        DataInputStream dataInput = null;
        List<GISVectorLayer> layers = new ArrayList<>();
        try {
            Path path = Paths.get(fileName);
            bufferInput = new BufferedInputStream(Files.newInputStream(path));
            dataInput = new DataInputStream(bufferInput);
            do {
                MyFileHeader mfh = new MyFileHeader();
                mfh.read(dataInput);
                SHAPETYPE shapetype = Enum.valueOf(SHAPETYPE.class, String.valueOf(mfh.shapeType));
                GISExtent extent = new GISExtent(mfh.minx, mfh.maxx, mfh.miny, mfh.maxy);
                String layerName = GISTools.readString(dataInput);
                List<GISField> fields = readFields(mfh.fieldCount, dataInput);
                GISVectorLayer layer = new GISVectorLayer(layerName, shapetype, extent, fields);
                readFeatures(layer, dataInput, mfh.featureCount);
                layers.add(layer);
            } while (dataInput.read() != -1);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                dataInput.close();
                bufferInput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return layers;
    }

    private static List<GISField> readFields(int fieldCount, DataInputStream br) throws ClassNotFoundException {
        List<GISField> fields = new ArrayList<>();
        for (int fieldindex = 0; fieldindex < fieldCount; fieldindex++) {
            fields.add(new GISField(br));
        }
        return fields;
    }

    private static void readFeatures(GISVectorLayer layer, DataInputStream randomAccessFile, int featureCount) throws IOException, ClassNotFoundException {
        for (int i = 0; i < featureCount; i++) {
            GISSpatial spatial = readSpatial(layer.shapeType, randomAccessFile);
            GISAttribute attribute = readAttribute(randomAccessFile, layer.fields);
            layer.addFeature(new GISFeature(spatial, attribute), false);
        }
    }
}
