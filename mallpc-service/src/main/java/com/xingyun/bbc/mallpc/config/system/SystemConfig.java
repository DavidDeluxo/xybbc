package com.xingyun.bbc.mallpc.config.system;

import com.xingyun.bbc.mallpc.common.utils.ApplicationContextHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 系统配置
 *
 * @author penglu
 * @version 1.0.0
 * @date 2019-08-24
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Configuration
@Import({ApplicationContextHelper.class})
public class SystemConfig {

    /**
     * 文件服务器地址
     */
    public static String fdfsHost;

    /**
     * bd二维码跳转地址
     */
    public static String qrCodeUrl;

    /**
     * bd推广地址
     */
    public static String extensionUrl;

    public SystemConfig(@Value("${fdfs.xy_host}") String fdfsHostValue, @Value("${bd.qrCodeUrl}") String qrCodeUrlValue, @Value("${bd.extensionUrlValue}") String extensionUrlValue) {
        fdfsHost = fdfsHostValue;
        qrCodeUrl = qrCodeUrlValue;
        extensionUrl = extensionUrlValue;
    }


}
