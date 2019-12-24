package org.walkgis.learngis.lesson20.basicclasses;

import java.io.IOException;
import java.io.RandomAccessFile;

public class GISField {
    public Class dataType;
    public String fieldName;

    public static class DBFField {
        public byte b1, b2, b3, b4, b5, b6, b7, b8, b9, b10, b11;
        public char fieldType;
        public int displacementInRecord;
        public byte lengthOfField;
        public byte numberOfDecimalPlaces;
        public byte Unused19, Unused20, Unused21, Unused22,
                Unused23, Unused24, Unused25, Unused26, Unused27,
                Unused28, Unused29, Unused30, Unused31, Unused32;
    }

    private DBFField dbfField;

    public GISField(Class dataType, String fieldName) {
        this.dataType = dataType;
        this.fieldName = fieldName;
    }

    public GISField(RandomAccessFile br) throws ClassNotFoundException {
        dbfField = (DBFField) GISTools.fromBytes(br, DBFField.class);
        byte[] bs = new byte[]{dbfField.b1, dbfField.b2, dbfField.b3, dbfField.b4, dbfField.b5,
                dbfField.b6, dbfField.b7, dbfField.b8, dbfField.b9, dbfField.b10, dbfField.b11};
        fieldName = GISTools.bytesToString(bs).trim();
        switch (dbfField.fieldType) {
            case 'C':  //字符型  允许输入各种字符
                dataType = Class.forName("java.lang.String");
                break;
            case 'D':  //日期型  用于区分年、月、日的数字和一个字符，内部存储按照YYYYMMDD格式。
                dataType = Class.forName("java.lang.String");
                break;
            case 'N':  //数值型
                if (dbfField.numberOfDecimalPlaces == 0)
                    dataType = Class.forName("java.lang.Integer");
                else
                    dataType = Class.forName("java.lang.Double");
                break;
            case 'F':
                dataType = Class.forName("java.lang.Double");
                break;
            case 'B':  //二进制 允许输入各种字符
                dataType = Class.forName("java.lang.String");
                break;
            case 'G':  //General or OLE
                dataType = Class.forName("java.lang.String");
                break;
            case 'L':  //逻辑型，表示没有初始化
                dataType = Class.forName("java.lang.String");
                break;
            case 'M': //Memo
                dataType = Class.forName("java.lang.String");
                break;
            default:
                break;
        }
    }

    public Object DBFValueToObject(RandomAccessFile br) throws IOException, ClassNotFoundException {
        byte[] temp = new byte[dbfField.lengthOfField];
        br.read(temp);
        String sv = GISTools.bytesToString(temp).trim();
        if (dataType == Class.forName("java.lang.String"))
            return sv;
        else if (dataType == Class.forName("java.lang.Double"))
            return Double.parseDouble(sv);
        else if (dataType == Class.forName("java.lang.Integer"))
            return Integer.parseInt(sv);
        return sv;
    }

    public Object myFileValueToObject(String value) throws ClassNotFoundException {
        if (dataType == Class.forName("java.lang.String"))
            return value;
        else if (dataType == Class.forName("java.lang.Double"))
            return Double.parseDouble(value);
        else if (dataType == Class.forName("java.lang.Integer"))
            return Integer.parseInt(value);
        //可持续写下去，处理所有可能的数据类型，比如Boolean，Int64等
        return value;
    }
}
