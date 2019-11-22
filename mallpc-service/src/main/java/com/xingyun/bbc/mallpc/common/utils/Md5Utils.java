package com.xingyun.bbc.mallpc.common.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author hekaijin
 * @Description:
 * @createTime: 2019-08-21 11:51
 */
public class Md5Utils {


    /**
     * MD5加密算法
     *
     * @param info 信息
     * @return java.lang.String
     */
    public static String toMd5(String info) {

        byte[] secretByte;
        try {
            secretByte = MessageDigest.getInstance("md5")
                    .digest(info.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("找不到md5算法");
        }
        StringBuilder md5Code = new StringBuilder(new BigInteger(1, secretByte).toString(16));
        for (int i = 0; i < 32 - md5Code.length(); i++) {
            md5Code.insert(0, "0");
        }
        return md5Code.toString();
    }
}
