package org.walkgis.learngis.lesson23.basicclasses;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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

        //写入数据
        public void write(DataOutputStream out) throws IOException {
            out.writeByte(this.b1);
            out.writeByte(this.b2);
            out.writeByte(this.b3);
            out.writeByte(this.b4);
            out.writeByte(this.b5);
            out.writeByte(this.b6);
            out.writeByte(this.b7);
            out.writeByte(this.b8);
            out.writeByte(this.b9);
            out.writeByte(this.b10);
            out.writeByte(this.b11);
            out.writeChar(this.fieldType);
            out.writeInt(this.displacementInRecord);
            out.writeByte(this.lengthOfField);
            out.writeByte(this.numberOfDecimalPlaces);
            out.writeByte(this.Unused19);
            out.writeByte(this.Unused20);
            out.writeByte(this.Unused21);
            out.writeByte(this.Unused22);
            out.writeByte(this.Unused23);
            out.writeByte(this.Unused24);
            out.writeByte(this.Unused25);
            out.writeByte(this.Unused26);
            out.writeByte(this.Unused27);
            out.writeByte(this.Unused28);
            out.writeByte(this.Unused29);
            out.writeByte(this.Unused30);
            out.writeByte(this.Unused31);
            out.writeByte(this.Unused32);
        }

        //读取数据
        public void read(DataInputStream input) throws IOException {
            this.b1 = input.readByte();
            this.b2 = input.readByte();
            this.b3 = input.readByte();
            this.b4 = input.readByte();
            this.b5 = input.readByte();
            this.b6 = input.readByte();
            this.b7 = input.readByte();
            this.b8 = input.readByte();
            this.b9 = input.readByte();
            this.b10 = input.readByte();
            this.b11 = input.readByte();
            this.fieldType = input.readChar();
            this.displacementInRecord = input.readInt();
            this.lengthOfField = input.readByte();
            this.numberOfDecimalPlaces = input.readByte();
            this.Unused19 = input.readByte();
            this.Unused20 = input.readByte();
            this.Unused21 = input.readByte();
            this.Unused22 = input.readByte();
            this.Unused23 = input.readByte();
            this.Unused24 = input.readByte();
            this.Unused25 = input.readByte();
            this.Unused26 = input.readByte();
            this.Unused27 = input.readByte();
            this.Unused28 = input.readByte();
            this.Unused29 = input.readByte();
            this.Unused30 = input.readByte();
            this.Unused31 = input.readByte();
            this.Unused32 = input.readByte();
        }
    }

    private DBFField dbfField;

    public GISField(Class dataType, String fieldName) {
        this.dataType = dataType;
        this.fieldName = fieldName;
    }

    public GISField(DataInputStream br) throws ClassNotFoundException {
        dbfField = new DBFField();
        try {
            dbfField.read(br);
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    public Object DBFValueToObject(DataInputStream br) throws IOException, ClassNotFoundException {
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
