package com.xingyun.bbc.mallpc.common.utils;

import com.xingyun.bbc.mallpc.config.system.SystemConfig;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-08-26
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public class FileUtils {

    /**
     * 根据文件相对路径返回绝对路径
     *
     * @param filePath 文件在FDFS上的相对路径，比如M00/00/00/wKgC3F1fr4CAeke6AAUwphutbZo294.jpg
     * @return 文件的绝对路径
     */
    public static String getFileUrl(String filePath) {
        return StringUtils.join(SystemConfig.fdfsHost, File.separator, filePath);
    }

}
