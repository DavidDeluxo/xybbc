package com.xingyun.bbc.mall.common.utils;

import com.xingyun.bbc.mallpc.common.ensure.Ensure;
import com.xingyun.bbc.mallpc.common.enums.PermissionEnums;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author nick
 */
@Slf4j
public class RequestHolder {

    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public static HttpServletResponse getResponse() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
    }

    /**
     * 获取用户ID
     *
     * @return
     */
    public static Long getUserId() {
        String adminId = getRequest().getHeader(PermissionEnums.ACCESS_TOKEN_XYID.getCode());
        Ensure.that(adminId).isNotBlank(MallPcExceptionCode.USER_NOT_LOGGED_IN);
        return Long.valueOf(adminId);
    }

    /**
     * 获取用户手机号码
     *
     * @return
     */
    public static String getUserMobile() {
        String subject = getRequest().getHeader(PermissionEnums.ACCESS_TOKEN_XYSUBJECT.getCode());
        Ensure.that(subject).isNotBlank(MallPcExceptionCode.USER_NOT_LOGGED_IN);
        return subject;
    }

    public static String loginIp() {
        HttpServletRequest request = getRequest();
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
