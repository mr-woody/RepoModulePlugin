package com.simple.utils;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author unknown
 * 该Utils包括MD5相关处理函数
 */
public class Md5Tools {
    /**
     * 定义出来16进制数组
     */
    private final static char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * 犯规md5加密后的信息
     *
     * @param abyte0 待处理内容
     * @param flag   是否将结果转换成大写
     * @return
     */
    public static String toMd5(@NonNull byte[] abyte0, boolean flag) {
        String res = "";
        try {
            MessageDigest messagedigest;
            messagedigest = MessageDigest.getInstance("MD5");
            messagedigest.reset();
            messagedigest.update(abyte0);
            res = toHexString(messagedigest.digest(), "", flag);
        } catch (NoSuchAlgorithmException e) {

        }
        return res;
    }

    /**
     * @param abyte0 待处理内容
     * @param s 结果追加内容
     * @param flag 是否大写
     * @return
     */
    private static String toHexString(byte[] abyte0, String s, boolean flag) {
        StringBuilder stringbuilder = new StringBuilder();
        byte[] abyte1 = abyte0;
        int i = abyte1.length;
        for (int j = 0; j < i; j++) {
            byte byte0 = abyte1[j];
            String s1 = Integer.toHexString(0xff & byte0);
            if (flag) {
                s1 = s1.toUpperCase();
            }
            if (s1.length() == 1) {
                stringbuilder.append("0");
            }
            stringbuilder.append(s1).append(s);
        }
        return stringbuilder.toString();
    }


    /**
     * 返回md5加密后的字符串
     *
     * @param s 处理内容
     * @return
     */
    @Nullable
    public static String getMD5String(@NonNull String s) {
        return getMD5String(s.getBytes());
    }

    /**
     * 返回md5加密后的字符串
     *
     * @param bytes
     * @return
     */
    @Nullable
    public static String getMD5String(@NonNull byte[] bytes) {
        MessageDigest messagedigest = null;
        try {
            messagedigest = MessageDigest.getInstance("MD5");
            messagedigest.update(bytes);
            return bufferToHex(messagedigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String bufferToHex(byte[] bytes) {
        return bufferToHex(bytes, 0, bytes.length);
    }

    private static String bufferToHex(byte[] bytes, int m, int n) {
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        int k = m + n;
        for (int l = m; l < k; l++) {
            appendHexPair(bytes[l], stringbuffer);
        }
        return stringbuffer.toString();
    }

    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        char c0 = HEX_DIGITS[(bt & 0xf0) >> 4];
        char c1 = HEX_DIGITS[bt & 0xf];
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }
}