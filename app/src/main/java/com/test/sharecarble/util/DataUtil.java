package com.test.sharecarble.util;

/**
 * Created by z on 2017/1/3 0003.
 */

public class DataUtil {

    /**
     * @param b 字节数组
     * @return 16进制字符串
     * @throws
     * @Title:bytes2HexString
     * @Description:字节数组转16进制字符串
     */
    public static String bytes2HexString(byte[] b) {
        StringBuffer result = new StringBuffer();
        String hex;
        for (int i = 0; i < b.length; i++) {
            hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            result.append(hex.toUpperCase());
        }
        return result.toString();
    }


    /**
     * byte转为二进制字符串
     *
     * @param b
     * @return
     */
    public static String byte2bits(byte b) {
        //来源:http://blog.csdn.net/qiantudou/article/details/49928423
        return Integer.toBinaryString((b & 0xFF) + 0x100).substring(1);
    }

    /**
     * 将二进制字符串转换回字节
     *
     * @param bString
     * @return
     */
    public static byte bit2byte(String bString) {
        byte result = 0;
        for (int i = bString.length() - 1, j = 0; i >= 0; i--, j++) {
            result += (Byte.parseByte(bString.charAt(i) + "") * Math.pow(2, j));
        }
        return result;
    }


    /**
     * 字符串转换为16进制字符串
     *
     * @param s
     * @return
     */
    public static String stringToHexString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = (int) s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str.toUpperCase();
    }

    /**
     * 16进制字符串转换为字符串
     *
     * @param s
     * @return
     */
    public static String hexStringToString(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        s = s.replace(" ", "");
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(
                        s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "gbk");
            new String();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }



    /**
     * 把16进制字符串转换成字节数组
     * @return byte[]
     */
//    public static byte[] hexStringToByte(String hex) {
//        int len = (hex.length() / 2);
//        byte[] result = new byte[len];
//        char[] achar = hex.toCharArray();
//        for (int i = 0; i < len; i++) {
//            int pos = i * 2;
//            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
//        }
//        return result;
//    }

    /**
     * 把16进制字符串转换成字节数组
     *
     * @return byte[]
     */
    public static byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        for (int i = 0, l = hex.length(); i < l; i += 2) {
            byte b = (byte) Integer.parseInt(hex.charAt(i) + "" + hex.charAt(i + 1),16);
            result[i / 2] = b;
        }
        return result;
    }

    private static int toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }
}
