package org.walkgis.learngis.lesson7;

public class Utils {
    /**
     * 转换short为byte
     *
     * @param b
     * @param s     需要转换的short
     * @param index
     */
    public static void short2Bytes(byte[] b, short s, int index) {
        b[index + 1] = (byte) (s >> 8);
        b[index + 0] = (byte) (s >> 0);
    }

    /**
     * 通过byte数组取到short
     *
     * @param b
     * @param index 第几位开始取
     * @return
     */
    public static short bytes2Short(byte[] b, int index) {
        return (short) (((b[index + 1] << 8) | b[index + 0] & 0xff));
    }

    /**
     * 将32位的int值放到4字节的byte数组
     *
     * @param num
     * @return
     */
    public static byte[] int2Bytes(int num) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (num & 0xff);
        bytes[1] = (byte) ((num >> 8) & 0xff);
        bytes[2] = (byte) ((num >> 16) & 0xff);
        bytes[3] = (byte) ((num >> 24) & 0xff);
        return bytes;
    }

    /**
     * 将4字节的byte数组转成一个int值
     *
     * @param bytes
     * @return
     */
    public static int bytes2Int(byte[] bytes, int index) {
        return (bytes[index + 0] & 0xff)
                | (bytes[index + 1] & 0xff) << 8
                | (bytes[index + 2] & 0xff) << 16
                | (bytes[index + 3] & 0xff) << 24;
    }

    /**
     * 将64位的long值放到8字节的byte数组
     *
     * @param num
     * @return 返回转换后的byte数组
     */
    public static byte[] longToBytes(long num) {
        byte[] result = new byte[8];
        result[0] = (byte) (num >>> 56);// 取最高8位放到0下标
        result[1] = (byte) (num >>> 48);// 取最高8位放到0下标
        result[2] = (byte) (num >>> 40);// 取最高8位放到0下标
        result[3] = (byte) (num >>> 32);// 取最高8位放到0下标
        result[4] = (byte) (num >>> 24);// 取最高8位放到0下标
        result[5] = (byte) (num >>> 16);// 取次高8为放到1下标
        result[6] = (byte) (num >>> 8); // 取次低8位放到2下标
        result[7] = (byte) (num); // 取最低8位放到3下标
        return result;
    }

    /**
     * 将8字节的byte数组转成一个long值
     *
     * @param byteArray
     * @return 转换后的long型数值
     */
    public static long bytesToLong(byte[] byteArray) {
        byte[] a = new byte[8];
        int i = a.length - 1, j = byteArray.length - 1;
        for (; i >= 0; i--, j--) {// 从b的尾部(即int值的低位)开始copy数据
            if (j >= 0)
                a[i] = byteArray[j];
            else
                a[i] = 0;// 如果b.length不足4,则将高位补0
        }
        // 注意此处和byte数组转换成int的区别在于，下面的转换中要将先将数组中的元素转换成long型再做移位操作，
        // 若直接做位移操作将得不到正确结果，因为Java默认操作数字时，若不加声明会将数字作为int型来对待，此处必须注意。
        long v0 = (long) (a[0] & 0xff) << 56;// &0xff将byte值无差异转成int,避免Java自动类型提升后,会保留高位的符号位
        long v1 = (long) (a[1] & 0xff) << 48;
        long v2 = (long) (a[2] & 0xff) << 40;
        long v3 = (long) (a[3] & 0xff) << 32;
        long v4 = (long) (a[4] & 0xff) << 24;
        long v5 = (long) (a[5] & 0xff) << 16;
        long v6 = (long) (a[6] & 0xff) << 8;
        long v7 = (long) (a[7] & 0xff);
        return v0 + v1 + v2 + v3 + v4 + v5 + v6 + v7;
    }

    /**
     * float转换byte
     *
     * @param bb
     * @param x
     * @param index
     */
    public static void float2Bytes(byte[] bb, float x, int index) {
        // byte[] b = new byte[4];
        int l = Float.floatToIntBits(x);
        for (int i = 0; i < 4; i++) {
            bb[index + i] = new Integer(l).byteValue();
            l = l >> 8;
        }
    }

    /**
     * 通过byte数组取得float
     *
     * @param b
     * @param index
     * @return
     */
    public static float bytes2Float(byte[] b, int index) {
        int l;
        l = b[index + 0];
        l &= 0xff;
        l |= ((long) b[index + 1] << 8);
        l &= 0xffff;
        l |= ((long) b[index + 2] << 16);
        l &= 0xffffff;
        l |= ((long) b[index + 3] << 24);
        return Float.intBitsToFloat(l);
    }

    public static byte[] double2Bytes(double d) {
        long value = Double.doubleToRawLongBits(d);
        byte[] byteRet = new byte[8];
        for (int i = 0; i < 8; i++) {
            byteRet[i] = (byte) ((value >> 8 * i) & 0xff);
        }
        return byteRet;
    }

    public static double bytes2Double(byte[] arr, int index) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value |= ((long) (arr[index + i] & 0xff)) << (8 * i);
        }
        return Double.longBitsToDouble(value);
    }
}
