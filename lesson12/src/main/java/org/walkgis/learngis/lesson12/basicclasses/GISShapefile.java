package org.walkgis.learngis.lesson12.basicclasses;

import com.linuxense.javadbf.DBFDataType;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import org.walkgis.learngis.lesson12.Utils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GISShapefile {
    /**
     * 100
     */
    class ShapefileHeader {
        //文件编号
        public int fileEncoding;
        //五个没有被使用的32位整数
        public int Unused1, Unused2, Unused3, Unused4, Unused5;
        //文件长度
        public int fileLength;
        //版本
        public int fileVersion;
        //图形类型
        public int ShapeType;
        //最小外接矩形
        public double Xmin, Ymin, Xmax, Ymax;
        //Z坐标值范围，分别代表Z坐标值的最小值、最大值
        public double zmin, zmax;
        //M坐标值范围、分布代表M坐标值得最小值、最大值
        public double mmin, mmax;
    }

    class RecordHeader {
        public int RecordNumber;
        public int RecordLength;
        public int ShapeType;
    }

    public ShapefileHeader readFileHeader(DataInputStream dataInputStream) {
        ShapefileHeader header = new ShapefileHeader();
        byte[] doubleByte = new byte[8];
        byte[] intByte = new byte[4];
        try {
            dataInputStream.read(intByte);
            header.fileEncoding = Utils.bytes2Int(intByte, 0);
            dataInputStream.read(intByte);
            header.Unused1 = Utils.bytes2Int(intByte, 0);
            dataInputStream.read(intByte);
            header.Unused2 = Utils.bytes2Int(intByte, 0);
            dataInputStream.read(intByte);
            header.Unused3 = Utils.bytes2Int(intByte, 0);
            dataInputStream.read(intByte);
            header.Unused4 = Utils.bytes2Int(intByte, 0);
            dataInputStream.read(intByte);
            header.Unused5 = Utils.bytes2Int(intByte, 0);
            dataInputStream.read(intByte);
            header.fileLength = Utils.bytes2Int(intByte, 0);
            dataInputStream.read(intByte);
            header.fileVersion = Utils.bytes2Int(intByte, 0);
            dataInputStream.read(intByte);
            header.ShapeType = Utils.bytes2Int(intByte, 0);
            dataInputStream.read(doubleByte);
            header.Xmin = Utils.bytes2Double(doubleByte, 0);
            dataInputStream.read(doubleByte);
            header.Ymin = Utils.bytes2Double(doubleByte, 0);
            dataInputStream.read(doubleByte);
            header.Xmax = Utils.bytes2Double(doubleByte, 0);
            dataInputStream.read(doubleByte);
            header.Ymax = Utils.bytes2Double(doubleByte, 0);

            dataInputStream.read(doubleByte);
            header.zmin = Utils.bytes2Double(doubleByte, 0);
            dataInputStream.read(doubleByte);
            header.zmax = Utils.bytes2Double(doubleByte, 0);
            dataInputStream.read(doubleByte);
            header.mmin = Utils.bytes2Double(doubleByte, 0);
            dataInputStream.read(doubleByte);
            header.mmax = Utils.bytes2Double(doubleByte, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return header;
    }

    public RecordHeader readRecordHeader(DataInputStream dataInputStream) {
        RecordHeader recordHeader = new RecordHeader();
        byte[] intByte = new byte[4];
        try {
            dataInputStream.read(intByte);
            recordHeader.RecordNumber = Utils.bytes2Int(intByte, 0);
            dataInputStream.read(intByte);
            recordHeader.RecordLength = Utils.bytes2Int(intByte, 0);
            dataInputStream.read(intByte);
            recordHeader.ShapeType = Utils.bytes2Int(intByte, 0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return recordHeader;
    }

    public int fromBigToLittle(int bigvalue) {
        byte[] bigbytes = Utils.int2Bytes(bigvalue);
        byte b2 = bigbytes[2];
        byte b3 = bigbytes[3];
        bigbytes[3] = bigbytes[0];
        bigbytes[2] = bigbytes[1];
        bigbytes[1] = b2;
        bigbytes[0] = b3;
        return Utils.bytes2Int(bigbytes, 0);
    }

    public GISLayer readShapefile(String shpfilename) {
        //打开文件和读取工具
        FileInputStream fsr = null;
        GISLayer layer = null;
        try {
            fsr = new FileInputStream(shpfilename + ".shp");
            DBFReader dbfReader = new DBFReader(new FileInputStream(shpfilename + ".dbf"), Charset.forName("UTF-8"));

            BufferedInputStream bufferedInputStream = new BufferedInputStream(fsr);
            DataInputStream br = new DataInputStream(bufferedInputStream);
            //读取文件头
            ShapefileHeader sfh = readFileHeader(br);
            //获得空间对象类型
            SHAPETYPE ShapeType = SHAPETYPE.getByValue(sfh.ShapeType);
            //获得空间范围
            GISExtent extent = new GISExtent(new GISVertex(sfh.Xmin, sfh.Ymin), new GISVertex(sfh.Xmax, sfh.Ymax));
            //初始化图层
            layer = new GISLayer(shpfilename, ShapeType, extent, getFields(dbfReader));
            Object[] rowValues;
            while (br.available() > 0 && ((rowValues = dbfReader.nextRecord()) != null)) {
                //读记录头
                RecordHeader rh = readRecordHeader(br);
                int recordLength = fromBigToLittle(rh.RecordLength) * 2 - 4;
                byte[] recordContent = new byte[recordLength];
                br.read(recordContent);
                //开始读实际的空间数据
                if (ShapeType == SHAPETYPE.point) {
                    GISPoint onepoint = readPoint(recordContent);
                    GISFeature onefeature = new GISFeature(onepoint, new GISAttribute(rowValues));
                    layer.addFeature(onefeature);
                } else if (ShapeType == SHAPETYPE.polyline) {
                    List<GISPolyline> lines = readLines(recordContent);
                    GISLayer finalLayer = layer;
                    Object[] finalRowValues = rowValues;
                    lines.forEach(line -> {
                        GISFeature onefeature = new GISFeature(line, new GISAttribute(finalRowValues));
                        finalLayer.addFeature(onefeature);
                    });
                } else if (ShapeType == SHAPETYPE.polygon) {
                    List<GISPolygon> polygons = readPolygons(recordContent);
                    GISLayer finalLayer1 = layer;
                    Object[] finalRowValues1 = rowValues;
                    polygons.forEach(polygon -> {
                        GISFeature onefeature = new GISFeature(polygon, new GISAttribute(finalRowValues1));
                        finalLayer1.addFeature(onefeature);
                    });
                }
            }
            //关闭读取工具和文件
            br.close();
            dbfReader.close();
            bufferedInputStream.close();
            fsr.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return layer;
    }

    private static List<GISField> getFields(DBFReader dbfReader) {
        List<GISField> fields = new ArrayList<>();

        for (int i = 0, size = dbfReader.getFieldCount(); i < size; i++) {
            DBFField field = dbfReader.getField(i);
            fields.add(new GISField(getType(field.getType()), field.getName()));
        }

        return fields;
    }

    private static Class getType(DBFDataType type) {
        Class clzz = String.class;
        switch (type) {
            case UNKNOWN:
            case CHARACTER:
                clzz = Character.class;
                break;
            case VARCHAR:
                clzz = String.class;
                break;
            case DOUBLE:
                clzz = Double.class;
                break;
            case LONG:
                clzz = Long.class;
                break;
            case AUTOINCREMENT:
                clzz = Integer.class;
                break;
            case TIMESTAMP:
            case DATE:
                clzz = Date.class;
                break;
            case NULL_FLAGS:
                clzz = Boolean.class;
                break;
            default:
                break;
        }
        return clzz;
    }

    private GISPoint readPoint(byte[] recordContent) {
        double x = Utils.bytes2Double(recordContent, 0);
        double y = Utils.bytes2Double(recordContent, 8);
        return new GISPoint(new GISVertex(x, y));
    }

    private List<GISPolyline> readLines(byte[] recordContent) {
        //自线段个数
        int N = Utils.bytes2Int(recordContent, 32);
        //坐标点数
        int M = Utils.bytes2Int(recordContent, 36);

        int[] parts = new int[N + 1];
        for (int i = 0; i < N; i++)
            parts[i] = Utils.bytes2Int(recordContent, 40 + i * 4);
        parts[N] = M;
        List<GISPolyline> lines = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            List<GISVertex> vertices = new ArrayList<>();
            for (int j = parts[i]; j < parts[i + 1]; j++) {
                double x = Utils.bytes2Double(recordContent, 40 + N * 4 + j * 16);
                double y = Utils.bytes2Double(recordContent, 40 + N * 4 + j * 16 + 8);
                vertices.add(new GISVertex(x, y));
            }
            lines.add(new GISPolyline(vertices));
        }
        return lines;
    }

    private List<GISPolygon> readPolygons(byte[] recordContent) {
        //自线段个数
        int N = Utils.bytes2Int(recordContent, 32);
        //坐标点数
        int M = Utils.bytes2Int(recordContent, 36);

        int[] parts = new int[N + 1];
        for (int i = 0; i < N; i++)
            parts[i] = Utils.bytes2Int(recordContent, 40 + i * 4);
        parts[N] = M;
        List<GISPolygon> polygons = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            List<GISVertex> vertices = new ArrayList<>();
            for (int j = parts[i]; j < parts[i + 1]; j++) {
                double x = Utils.bytes2Double(recordContent, 40 + N * 4 + j * 16);
                double y = Utils.bytes2Double(recordContent, 40 + N * 4 + j * 16 + 8);
                vertices.add(new GISVertex(x, y));
            }
            polygons.add(new GISPolygon(vertices));
        }
        return polygons;
    }
}
