package com.xingyun.bbc.mallpc.common.utils;

import com.xingyun.bbc.mallpc.config.system.SystemConfig;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

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

    public static void download(String fileName, File file, HttpServletResponse response) throws Exception {
        OutputStream out = null;
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            response.setContentType("multipart/form-data");
            response.addHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes("UTF-8"), "ISO8859-1"));
            String len = String.valueOf(file.length());
            response.setHeader("Content-Length", len);
            out = response.getOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = in.read(b)) != -1) {
                out.write(b, 0, n);
            }
            out.flush();
        } catch (FileNotFoundException e) {
            throw new Exception("文件不存在");
        } catch (IOException e) {
            throw new Exception("文件下载失败");
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                System.out.println("文件下载失败");
            }
        }
    }
}
