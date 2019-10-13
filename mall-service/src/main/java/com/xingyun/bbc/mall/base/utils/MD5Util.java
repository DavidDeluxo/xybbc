package com.xingyun.bbc.mall.base.utils;

import java.security.MessageDigest;

/**
 * @author pengaoluo
 * @version 1.0.0
 * @date 2019/8/26
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public class MD5Util {

    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static final String SALT = "@#$$fdsdsijlg%(#*^23434rfdgjmo676yhhnj";

    /**
     * 遍历byte数组，转化为16位进制的字符
     *
     * @param b
     * @return
     */
    private static String byteArrayToHexString(byte[] b) {
        char[] chars = new char[b.length * 2];
        for (int i = 0; i < b.length; i++) {
            int n = b[i];
            if (n < 0) {
                n += 256;
            }
            chars[i * 2] = HEX_DIGITS[n >>> 4];
            chars[i * 2 + 1] = HEX_DIGITS[n & 0xf];
        }
        return new String(chars);
    }

    /**
     * 返回大写MD5
     *
     * @param origin
     * @param charsetName
     * @return
     */
    public static String MD5Encode(String origin, String charsetName) {
        String resultString = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            resultString = byteArrayToHexString(md.digest(origin.getBytes(charsetName)));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return resultString;
    }

    public static String MD5EncodeUtf8(String origin) {
        origin = origin + SALT;
        return MD5Encode(origin, "utf-8");
    }

}
