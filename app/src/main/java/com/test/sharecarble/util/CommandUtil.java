package com.test.sharecarble.util;


import java.io.UnsupportedEncodingException;
import java.util.Random;

/**
 * 发命令工具类
 * Created by z on 2017/3/24 0024.
 */

public class CommandUtil {

    //0004018001AABBCCDDEEFF3132333235363738EE   //设备认证
    //0005018006F0E0CCDDEEFF3132333235363738EE   //开锁
    //0005018006F1E1CCDDEEFF3132333235363738EE   //关锁
    //0005018006F2E2CCDDEEFF3132333235363738EE   //寻车（闪一闪）
    //0004018007AABBCCDDEEFF3132333235363738EE   //状态查询


    public static final byte[] TEMP_USER = new byte[]{(byte) 0x80, (byte) 0x80};

    public static byte[] MAC_DATA = new byte[6];

    public static byte CMD_NUM = 0x00;//序列号

    public static void setMacData(String mac) {
        if (mac != null) {
            String[] address = mac.split(":");
            for (int i = 0; i < address.length; i++) {
                MAC_DATA[i] = (byte) Integer.parseInt(address[i], 16);
            }
        }
    }

    public static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) new Random().nextInt(255);
        }
        return bytes;
    }

    /**
     * 设备验证
     */
    public static byte[] deviceCheck() {
        byte[] data = new byte[20];
        CMD_NUM = 0X00;
        data[0] = CMD_NUM;
        CMD_NUM++;
        data[1] = 0x04;
        data[2] = TEMP_USER[0];
        data[3] = TEMP_USER[1];
        data[4] = 0x01;
        System.arraycopy(MAC_DATA, 0, data, 5, MAC_DATA.length);
        System.arraycopy(randomBytes(8), 0, data, 11, 8);
        data[data.length - 1] = CRC8.calcCrc8(data);


        return data;
    }

    /**
     * 上锁
     */
    public static byte[] closeClock() {
        byte[] data = new byte[20];
        data[0] = CMD_NUM;
        CMD_NUM++;
        data[1] = 0x05;
        data[2] = TEMP_USER[0];
        data[3] = TEMP_USER[1];
        data[4] = 0x06;
        data[5] = (byte) 0xE0;
        data[6] = (byte) 0xE0;

        System.arraycopy(randomBytes(12), 0, data, 7, 12);
        data[data.length - 1] = CRC8.calcCrc8(data);

        return data;
    }

    /**
     * 开锁
     */
    public static byte[] openClock() {
        byte[] data = new byte[20];
        data[0] = CMD_NUM;
        CMD_NUM++;
        data[1] = 0x05;
        data[2] = TEMP_USER[0];
        data[3] = TEMP_USER[1];
        data[4] = 0x06;
        data[5] = (byte) 0xE1;
        data[6] = (byte) 0xE1;

        System.arraycopy(randomBytes(12), 0, data, 7, 12);
        data[data.length - 1] = CRC8.calcCrc8(data);

        return data;
    }

    /**
     * 找车锁
     */
    public static byte[] looking() {
        byte[] data = new byte[20];
        data[0] = CMD_NUM;
        CMD_NUM++;
        data[1] = 0x05;
        data[2] = TEMP_USER[0];
        data[3] = TEMP_USER[1];
        data[4] = 0x06;
        data[5] = (byte) 0xE2;
        data[6] = (byte) 0xE2;

        System.arraycopy(randomBytes(12), 0, data, 7, 12);
        data[data.length - 1] = CRC8.calcCrc8(data);

        return data;
    }

    /**
     * 预约
     */
    public static byte[] booking() {
        byte[] data = new byte[20];
        data[0] = CMD_NUM;
        CMD_NUM++;
        data[1] = 0x05;
        data[2] = TEMP_USER[0];
        data[3] = TEMP_USER[1];
        data[4] = 0x06;
        data[5] = (byte) 0xE3;
        data[6] = (byte) 0xE3;

        System.arraycopy(randomBytes(12), 0, data, 7, 12);
        data[data.length - 1] = CRC8.calcCrc8(data);

        return data;
    }

    /**
     * 取消预约
     */
    public static byte[] unBooking() {
        byte[] data = new byte[20];
        data[0] = CMD_NUM;
        CMD_NUM++;
        data[1] = 0x05;
        data[2] = TEMP_USER[0];
        data[3] = TEMP_USER[1];
        data[4] = 0x06;
        data[5] = (byte) 0xE4;
        data[6] = (byte) 0xE4;

        System.arraycopy(randomBytes(12), 0, data, 7, 12);
        data[data.length - 1] = CRC8.calcCrc8(data);

        return data;
    }

    /**
     * 查询锁的状态
     */
    public static byte[] status() {
        byte[] data = new byte[20];
        data[0] = CMD_NUM;
        CMD_NUM++;
        data[1] = 0x04;
        data[2] = TEMP_USER[0];
        data[3] = TEMP_USER[1];
        data[4] = 0x07;

        System.arraycopy(randomBytes(14), 0, data, 5, 14);
        data[data.length - 1] = CRC8.calcCrc8(data);

        return data;
    }

    /**
     * 设置IP地址
     */
    public static byte[] setIp(String ip) {
        byte[] data = new byte[20];
        data[0] = CMD_NUM;
        CMD_NUM++;
        data[1] = 0x05;
        data[2] = TEMP_USER[0];
        data[3] = TEMP_USER[1];
        data[4] = 0x08;
        String[] ips = ip.split("\\.");
        data[5] = (byte) Integer.parseInt(ips[0]);
        data[6] = (byte) Integer.parseInt(ips[1]);
        data[7] = (byte) Integer.parseInt(ips[2]);
        data[8] = (byte) Integer.parseInt(ips[3]);

        System.arraycopy(randomBytes(10), 0, data, 9, 10);
        data[data.length - 1] = CRC8.calcCrc8(data);

        return data;
    }

    /**
     * 设置端口
     */
    public static byte[] setPort(String port) {
        byte[] data = new byte[20];
        data[0] = CMD_NUM;
        CMD_NUM++;
        data[1] = 0x05;
        data[2] = TEMP_USER[0];
        data[3] = TEMP_USER[1];
        data[4] = 0x09;
        for (int i = 5 - port.length(); i > 0; i--) {
            port = "0" + port;
        }

//        data[5] = (byte) Integer.parseInt(Integer.toHexString(Integer.parseInt(port.substring(0, 2))), 16);
//        data[6] = (byte) Integer.parseInt(Integer.toHexString(Integer.parseInt(port.substring(2, 4))), 16);
        try {
            byte[] ports = port.getBytes("UTF-8");
            System.arraycopy(ports, 0, data, 5, ports.length);
            System.arraycopy(randomBytes(14 - ports.length), 0, data, 5 + ports.length, 14 - ports.length);
            data[data.length - 1] = CRC8.calcCrc8(data);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        return data;
    }

    /**
     * 设置ID
     */
    public static byte[] setId(String id) {
        byte[] data = new byte[20];
        data[0] = CMD_NUM;
        CMD_NUM++;
        data[1] = 0x05;
        data[2] = TEMP_USER[0];
        data[3] = TEMP_USER[1];
        data[4] = (byte) 0x0A;

//        data[5] = (byte) Integer.parseInt(id.substring(0, 2));
//        data[6] = (byte) Integer.parseInt(id.substring(2, 4));
//        data[7] = (byte) Integer.parseInt(id.substring(4, 6));
//        data[8] = (byte) Integer.parseInt(id.substring(6, 8));
//        data[9] = (byte) Integer.parseInt(id.substring(8, 10));
//        data[10] = (byte) Integer.parseInt(id.substring(10, 12));
//        data[11] = (byte) Integer.parseInt(id.substring(12, 14));
//        data[12] = (byte) Integer.parseInt(id.substring(14, 16));
//        data[13] = (byte) Integer.parseInt(id.substring(16, 18));
//        data[14] = (byte) Integer.parseInt(id.substring(18, 20));

        try {
            byte[] ids = id.getBytes("UTF-8");
            System.arraycopy(ids, 0, data, 5, ids.length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.arraycopy(randomBytes(4), 0, data, 15, 4);
        data[data.length - 1] = CRC8.calcCrc8(data);

        return data;
    }

}
