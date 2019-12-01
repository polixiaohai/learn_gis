package org.walkgis.learngis.lesson5.basicclasses;

import org.walkgis.learngis.lesson5.Utils;

import java.io.*;

public class GISShapefile {
    /**
     * 100
     */
    class ShapefileHeader {
        //文件编号
        public int Unused1;
        //五个没有被使用的32位整数
        public int Unused2, Unused3, Unused4, Unused5, Unused6;
        //文件长度
        public int Unused7;
        //版本
        public int Unused8;
        //图形类型
        public int ShapeType;
        //最小外接矩形
        public double Xmin, Ymin, Xmax, Ymax;
        //Z坐标值范围，分别代表Z坐标值的最小值、最大值
        public double Unused9, Unused10;
        //M坐标值范围、分布代表M坐标值得最小值、最大值
        public double Unused11, Unused12;
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
            header.Unused1 = Utils.bytes2Int(intByte);
            dataInputStream.read(intByte);
            header.Unused2 = Utils.bytes2Int(intByte);
            dataInputStream.read(intByte);
            header.Unused3 = Utils.bytes2Int(intByte);
            dataInputStream.read(intByte);
            header.Unused4 = Utils.bytes2Int(intByte);
            dataInputStream.read(intByte);
            header.Unused5 = Utils.bytes2Int(intByte);
            dataInputStream.read(intByte);
            header.Unused6 = Utils.bytes2Int(intByte);
            dataInputStream.read(intByte);
            header.Unused7 = Utils.bytes2Int(intByte);
            dataInputStream.read(intByte);
            header.Unused8 = Utils.bytes2Int(intByte);
            dataInputStream.read(intByte);
            header.ShapeType = Utils.bytes2Int(intByte);
            dataInputStream.read(doubleByte);
            header.Xmin = Utils.bytes2Double(doubleByte, 0);
            dataInputStream.read(doubleByte);
            header.Ymin = Utils.bytes2Double(doubleByte, 0);
            dataInputStream.read(doubleByte);
            header.Xmax = Utils.bytes2Double(doubleByte, 0);
            dataInputStream.read(doubleByte);
            header.Ymax = Utils.bytes2Double(doubleByte, 0);

            dataInputStream.read(doubleByte);
            header.Unused9 = Utils.bytes2Double(doubleByte, 0);
            dataInputStream.read(doubleByte);
            header.Unused10 = Utils.bytes2Double(doubleByte, 0);
            dataInputStream.read(doubleByte);
            header.Unused11 = Utils.bytes2Double(doubleByte, 0);
            dataInputStream.read(doubleByte);
            header.Unused12 = Utils.bytes2Double(doubleByte, 0);

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
            recordHeader.RecordNumber = Utils.bytes2Int(intByte);
            dataInputStream.read(intByte);
            recordHeader.RecordLength = Utils.bytes2Int(intByte);
            dataInputStream.read(intByte);
            recordHeader.ShapeType = Utils.bytes2Int(intByte);
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
        return Utils.bytes2Int(bigbytes);
    }


    public GISLayer readShapefile(String shpfilename) {
        //打开文件和读取工具
        FileInputStream fsr = null;
        GISLayer layer = null;
        try {
            fsr = new FileInputStream(shpfilename);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fsr);
            DataInputStream br = new DataInputStream(bufferedInputStream);
            //读取文件头
            ShapefileHeader sfh = readFileHeader(br);
            //获得空间对象类型
            SHAPETYPE ShapeType = SHAPETYPE.getByValue(sfh.ShapeType);
            //获得空间范围
            GISExtent extent = new GISExtent(new GISVertex(sfh.Xmin, sfh.Ymin), new GISVertex(sfh.Xmax, sfh.Ymax));
            //初始化图层
            layer = new GISLayer(shpfilename, ShapeType, extent);
            while (br.available() > 0) {
                //读记录头
                RecordHeader rh = readRecordHeader(br);
                int recordLength = fromBigToLittle(rh.RecordLength) * 2 - 4;
                byte[] recordContent = new byte[recordLength];
                br.read(recordContent);
                //开始读实际的空间数据
                if (ShapeType == SHAPETYPE.point) {
                    GISPoint onepoint = readPoint(recordContent);
                    GISFeature onefeature = new GISFeature(onepoint, new GISAttribute());
                    layer.addFeature(onefeature);
                }
            }
            //关闭读取工具和文件
            br.close();
            bufferedInputStream.close();
            fsr.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return layer;
    }

    public GISPoint readPoint(byte[] recordContent) {
        double x = Utils.bytes2Double(recordContent, 0);
        double y = Utils.bytes2Double(recordContent, 8);
        return new GISPoint(new GISVertex(x, y));
    }
}
