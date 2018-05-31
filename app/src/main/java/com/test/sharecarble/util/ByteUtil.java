package com.test.sharecarble.util;

/**
 * byte数组解析工具类
 * <p>
 * 包含了大小端模式下byte数组与short、int、long类型互转
 * </p>
 * Created by z on 2017/9/9.
 */
public class ByteUtil {
    public static final short byteArrayToShort_Little(byte[] byt, int nBeginPos) {
        return (short) (255 & byt[nBeginPos] | (255 & byt[nBeginPos + 1]) << 8);
    }

    public static final int byteArrayToInt_Little(byte[] byt, int nBeginPos) {
        return 255 & byt[nBeginPos] | (255 & byt[nBeginPos + 1]) << 8 | (255 & byt[nBeginPos + 2]) << 16 | (255 & byt[nBeginPos + 3]) << 24;
    }

    public static final long byteArrayToLong_Little2(byte[] byt, int nBeginPos) {
        long l = 0L;

        for (int i = 0; i < 4; ++i) {
            l |= (255L & (long) byt[nBeginPos + i]) << 8 * i;
        }

        return l;
    }

    public static final int byteArrayToInt_Little(byte[] byt) {
        return byt.length == 1 ? 255 & byt[0] : (byt.length == 2 ? 255 & byt[0] | (255 & byt[1]) << 8 : (byt.length == 4 ? 255 & byt[0] | (255 & byt[1]) << 8 | (255 & byt[2]) << 16 | (255 & byt[3]) << 24 : 0));
    }

    public static final long byteArrayToLong_Little(byte[] byt, int nBeginPos) {
        return (long) (255 & byt[nBeginPos] | (255 & byt[nBeginPos + 1]) << 8 | (255 & byt[nBeginPos + 2]) << 16 | (255 & byt[nBeginPos + 3]) << 24 | (255 & byt[nBeginPos + 4]) << 32 | (255 & byt[nBeginPos + 5]) << 40 | (255 & byt[nBeginPos + 6]) << 48 | (255 & byt[nBeginPos + 7]) << 56);
    }

    public static final int byteArrayToInt_Big(byte[] byt) {
        return byt.length == 1 ? 255 & byt[0] : (byt.length == 2 ? (255 & byt[0]) << 8 | 255 & byt[1] : (byt.length == 4 ? (255 & byt[0]) << 24 | (255 & byt[1]) << 16 | (255 & byt[2]) << 8 | 255 & byt[3] : 0));
    }

    public static final byte[] longToByteArray_Little(long value) {
        return new byte[]{(byte) ((int) value), (byte) ((int) (value >>> 8)), (byte) ((int) (value >>> 16)), (byte) ((int) (value >>> 24)), (byte) ((int) (value >>> 32)), (byte) ((int) (value >>> 40)), (byte) ((int) (value >>> 48)), (byte) ((int) (value >>> 56))};
    }

    public static final byte[] intToByteArray_Little(int value) {
        return new byte[]{(byte) value, (byte) (value >>> 8), (byte) (value >>> 16), (byte) (value >>> 24)};
    }

    public static final byte[] intToByteArray_Big(int value) {
        return new byte[]{(byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value};
    }

    public static final byte[] shortToByteArray_Little(short value) {
        return new byte[]{(byte) value, (byte) (value >>> 8)};
    }

    public static final byte[] shortToByteArray_Big(short value) {
        return new byte[]{(byte) (value >>> 8), (byte) value};
    }

    public static final short[] byteArray2shortArray_Little(byte[] b, int length) {
        short[] buf = new short[length / 2];

        for (int i = 0; i < length / 2; ++i) {
            buf[i] = byteArrayToShort_Little(b, i * 2);
        }

        return buf;
    }

    public static final byte[] shortArray2byteArray_Little(short[] s, int length) {
        byte[] buf = new byte[length * 2];

        for (int i = 0; i < length; ++i) {
            short s0 = s[i];
            byte[] b = shortToByteArray_Little(s0);
            buf[i * 2 + 0] = b[0];
            buf[i * 2 + 1] = b[1];
        }

        return buf;
    }

    public static final long bytes2Long(byte[] data, int length) {
        Object bData = null;
        byte[] var8 = reverse(data, length);
        short mask = 255;
        boolean temp = false;
        long n = 0L;

        for (int i = 0; i < length; ++i) {
            n <<= 8;
            int var9 = var8[i] & mask;
            n |= (long) var9;
        }

        return n;
    }

    public static byte[] reverse(byte[] data, int length) {
        int nSize = length;

        for (int i = 0; i < nSize / 2; ++i) {
            byte temp = data[i];
            data[i] = data[nSize - 1 - i];
            data[nSize - 1 - i] = temp;
        }

        return data;
    }
}
