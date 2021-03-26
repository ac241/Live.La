
package com.acel.streamlivetool.platform.impl.douyu.danmu;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * @author https://github.com/duzouw/WebSocketClient
 */

public final class DanmuTool {


    /**
     * 生成 UUID
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }

    /**
     * 以大端模式将 int 转成 byte[]
     */
    public static byte[] intToBytesBig(int value) {
        return new byte[]{
                (byte) ((value >> 24) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) (value & 0xFF)
        };
    }

    /**
     * 以小端模式将 int 转成 byte[]
     * <p>
     * int = 2340
     * 00000000 00000000 00001001 00100100
     * 0xff
     * 00000000 00000000 00000000 11111111
     * <p>
     * [0]
     * 00000000 00000000 00001001 00100100
     * 00000000 00000000 00000000 11111111
     * -----------------------------------
     * 00000000 00000000 00000000 00100100 | 36
     * <p>
     * [1]
     * 00000000 00000000 00000000 00001001
     * 00000000 00000000 00000000 11111111
     * -----------------------------------
     * 00000000 00000000 00000000 00001001 | 09
     * <p>
     * [2]
     * 00000000 00000000 00000000 00000000 | 00
     * <p>
     * [3]
     * 00000000 00000000 00000000 00000000 | 00
     */
    public static byte[] intToBytesLittle(int value) {
        return new byte[]{
                (byte) (value & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 24) & 0xFF)
        };
    }

    /**
     * 以大端模式将 byte[]转成 int
     */
    public static int bytesToIntBig(byte[] src) {
        return (((src[0] & 0xFF) << 24)
                | ((src[1] & 0xFF) << 16)
                | ((src[2] & 0xFF) << 8)
                | (src[3] & 0xFF));
    }

    /**
     * 以小端模式将 byte[] 转成 int
     */
    public static int bytesToIntLittle(byte[] bytes) {
        return ((bytes[0] & 0xFF)
                | ((bytes[1] & 0xFF) << 8)
                | ((bytes[2] & 0xFF) << 16)
                | ((bytes[3] & 0xFF) << 24));
    }

    /**
     * hex转byte数组
     */
    public static byte[] hexToByte(String hex) {
        int m, n;
        int byteLen = hex.length() / 2;
        byte[] ret = new byte[byteLen];
        for (int i = 0; i < byteLen; i++) {
            m = i * 2 + 1;
            n = m + 1;
            int intVal = Integer.decode("0x" + hex.substring(i * 2, m) + hex.substring(m, n));
            ret[i] = (byte) intVal;
        }
        return ret;
    }

    /**
     * byte数组转hex
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(aByte & 0xFF);
            if (hex.length() < 2) sb.append(0);
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * Zlib 压缩
     */
    public static byte[] compressZlib(byte[] data) {
        Deflater deflater = new Deflater();
        deflater.reset();
        deflater.setInput(data);
        deflater.finish();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!deflater.finished()) {
                int i = deflater.deflate(buf);
                byteArrayOutputStream.write(buf, 0, i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        deflater.end();
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Zlib 解压
     */
    public static byte[] decompressZlib(byte[] data) {
        Inflater inflater = new Inflater();
        inflater.reset();
        inflater.setInput(data);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!inflater.finished()) {
                int i = inflater.inflate(buf);
                byteArrayOutputStream.write(buf, 0, i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        inflater.end();
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 消息解析
     */
    public static String translate(String content, String... contents) {
        if (contents == null || contents.length == 0 || contents.length % 2 != 0) return content;
        else {
            for (int i = 0; i < contents.length; i++) {
                if (i % 2 == 0)
                    content = content.replaceAll("\\$\\{" + contents[i] + "}", contents[i + 1]);
            }
            return content;
        }
    }

    /**
     * byte 数组和并
     */
    public static void arrayJoinByte(byte[] a, byte[]... b) {
        int count = 0;
        for (byte[] bytes : b) for (byte by : bytes) a[count++] = by;
    }

    /**
     * 数组获取指定长度的内容
     */
    public static byte[] arrayIndexesByte(byte[] bytes, int index, int len) {
        byte[] result = new byte[len];
        for (int i = index; i < index + len; i++) result[i - index] = bytes[index];
        return result;
    }

    /**
     * 全局注入 gson 对象
     */
    private static final Gson g = new Gson();

    /**
     * Object 序列化 JSON 工具
     */
    public static String toJson(Object o) {
        return g.toJson(o);
    }

    /**
     * JSON 反序列工具
     */
    public static <T> T fromJson(String content) {
        return g.fromJson(content, new TypeToken<T>() {
        }.getType());
    }

    /**
     * JSON 反序列工具
     */
    public static <T> T fromJson(String content, Class<T> clazz) {
        return g.fromJson(content, clazz);
    }

}
