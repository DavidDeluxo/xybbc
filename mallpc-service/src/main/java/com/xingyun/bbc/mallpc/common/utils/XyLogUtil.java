package com.xingyun.bbc.mallpc.common.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author lchm
 * @version 1.0.0
 * @date 2019/11/27 21:16
 * @description: 错误日志完整打印
 * @package com.xingyun.bbc.order.base.utils
 */
public class XyLogUtil {

    private XyLogUtil() {
    }

    /**
     * 错误的堆栈信息转成string
     *
     * @param e
     * @return
     */
    public static String logStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        String errorStr = sw.toString();
        pw.flush();
        pw.close();
        return errorStr;
    }
}