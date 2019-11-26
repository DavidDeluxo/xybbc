package com.xingyun.bbc.mallpc.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-10-22
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Data
@Accessors(chain = true)
public class MallPcLogVo implements Serializable {

    private static final long serialVersionUID = -7232385364250809841L;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求参数
     */
    private String requestParam;

    /**
     * 执行时间，毫秒
     */
    private long executeTime;

    /**
     * 响应码
     */
    private String responseCode;

    /**
     * 响应描述
     */
    private String responseMsg;

    /**
     * 响应数据
     */
    private String responseData;

    /**
     * 响应扩展数据
     */
    private String responseExtraData;

}
