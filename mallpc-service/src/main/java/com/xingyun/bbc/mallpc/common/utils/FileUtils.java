package com.xingyun.bbc.mallpc.common.utils;

import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import com.xingyun.bbc.mallpc.config.system.SystemConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

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
            throw new BizException(MallPcExceptionCode.FILE_NOT_EXIST);
        } catch (IOException e) {
            throw new BizException(MallPcExceptionCode.FILE_NOT_EXIST);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                throw new BizException(MallPcExceptionCode.FILE_NOT_EXIST);
            }
        }
    }

    public static byte[] url2Byte(String urlStr) {
        InputStream is = null;
        ByteArrayOutputStream os = null;
        byte[] buff = new byte[1024];
        int len = 0;
        try {
            URL url = new URL(UriUtils.encodePath(urlStr, "UTF-8"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "plain/text;charset=" + "UTF-8");
            conn.setRequestProperty("charset", "UTF-8");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            conn.setReadTimeout(500);
            conn.connect();
            is = conn.getInputStream();
            os = new ByteArrayOutputStream();
            while ((len = is.read(buff)) != -1) {
                os.write(buff, 0, len);
            }
            return os.toByteArray();
        } catch (IOException e) {
            throw new BizException(MallPcExceptionCode.FILE_NOT_EXIST);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    throw new BizException(MallPcExceptionCode.FILE_NOT_EXIST);
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    throw new BizException(MallPcExceptionCode.FILE_NOT_EXIST);
                }
            }
        }
    }

    public static void download(byte[] byteArr, HttpServletResponse response, String fileName) throws Exception {
        if (StringUtils.isEmpty(fileName)) {
            throw new BizException(MallPcExceptionCode.FILE_NOT_EXIST);
        }
        if (null == byteArr) {
            throw new BizException(MallPcExceptionCode.FILE_NOT_EXIST);
        }
        response.setContentType("multipart/form-data");
        response.addHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes("UTF-8"), "ISO8859-1"));
        String len = String.valueOf(byteArr.length);
        response.setHeader("Content-Length", len);
        try (ServletOutputStream out = response.getOutputStream()) {
            out.write(byteArr);
            out.flush();
        } catch (IOException e) {
            throw new BizException(MallPcExceptionCode.FILE_NOT_EXIST);
        }
    }

    public static byte[] file2byte(File file) {
        byte[] buffer = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            throw new BizException(MallPcExceptionCode.FILE_NOT_EXIST);
        } catch (IOException e) {
            throw new BizException(MallPcExceptionCode.FILE_NOT_EXIST);
        }
        return buffer;
    }

}
