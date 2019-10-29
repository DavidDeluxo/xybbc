package com.xingyun.bbc.mall.base.utils;

import com.xingyun.bbc.mall.common.ensure.Ensure;
import com.xingyun.bbc.mall.common.exception.MallExceptionCode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class FileUtil {

    public static String getRemoteFile(String url){
        Ensure.that(url).isNotBlank(MallExceptionCode.PARAM_ERROR);
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null){
                builder.append(line);
            }
            return builder.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
