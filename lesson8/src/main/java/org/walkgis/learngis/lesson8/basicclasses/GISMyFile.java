package org.walkgis.learngis.lesson8.basicclasses;

import org.walkgis.learngis.lesson8.Utils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;

public class GISMyFile {
    class MyFileHeader {
        public double minx, miny, maxx, maxy;
        public int featureCount, shapeType, fieldCount;
    }

    public enum ALLTYPES {
        java_lang_Boolean(0),
        java_lang_Byte(1),
        java_lang_Character(2),
        java_lang_Double(3),
        java_lang_Integer(04),
        java_lang_Long(5),
        java_lang_Short(6),
        java_lang_String(7);
        private Integer value;

        ALLTYPES(Integer value) {
            this.value = value;
        }


        public Integer getValue() {
            return value;
        }
    }

    private void writeFields(List<GISField> fields, DataOutputStream bw) {
        for (int i = 0; i < fields.size(); i++) {
            try {
                GISField field = fields.get(i);
                bw.write(typeToInt(field.dataType));
                writeString(field.fileName, bw);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int typeToInt(Class clzz) {
        ALLTYPES alltypes = Enum.valueOf(ALLTYPES.class, clzz.toString().replace(".", "_"));
        return alltypes.getValue();
    }

    private void writeFileHeader(GISLayer layer, DataOutputStream bw) {
        MyFileHeader mfh = new MyFileHeader();
        mfh.minx = layer.extent.getMinX();
        mfh.miny = layer.extent.getMinY();
        mfh.maxx = layer.extent.getMaxX();
        mfh.maxy = layer.extent.getMaxY();

        mfh.featureCount = layer.features.size();
        mfh.shapeType = layer.shapeType.getValue();
        mfh.fieldCount = layer.fields.size();
        try {
            bw.write(Utils.double2Bytes(mfh.minx));
            bw.write(Utils.double2Bytes(mfh.miny));
            bw.write(Utils.double2Bytes(mfh.maxx));
            bw.write(Utils.double2Bytes(mfh.maxy));

            bw.write(Utils.int2Bytes(mfh.featureCount));
            bw.write(Utils.int2Bytes(mfh.shapeType));
            bw.write(Utils.int2Bytes(mfh.fieldCount));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeString(String s, DataOutputStream bw) {
        try {
            bw.write(stringLength(s));
            byte[] bytes = s.getBytes(Charset.forName("UTF-8"));
            bw.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int stringLength(String s) {
        int chineseCount = 0;
        byte[] bs = new byte[0];
        bs = s.getBytes(Charset.forName("US-ASCII"));
        for (byte b : bs)
            if (b == 0x3F) chineseCount++;
        return chineseCount + bs.length;
    }

    private void writeMultiVertex(List<GISVertex> vertices, DataOutputStream bw) {
        try {
            bw.write(vertices.size());
            for (int i = 0, size = vertices.size(); i < size; i++)
                vertices.get(i).writeVertex(bw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void wirteFile(GISLayer layer, String fileName) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            DataOutputStream bw = new DataOutputStream(fileOutputStream);

            writeFileHeader(layer, bw);
            writeString(layer.name, bw);
            writeFields(layer.fields, bw);
            writeFileHeader(layer, bw);

            bw.close();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
