package com.xingyun.bbc.mallpc.common.utils;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.model.vo.MallPcLogVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-10-22
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Slf4j
public class MallPcLogHelper {

    private MallPcLogHelper() {
    }

    public static final MallPcLogHelper getInstance() {
        return MallPcLogHelperHolder.MALL_PC_LOG_HELPER;
    }

    private static final class MallPcLogHelperHolder {
        private static final MallPcLogHelper MALL_PC_LOG_HELPER = new MallPcLogHelper();
    }

    public void outputLog(Result result, Object requestParam, long executeTime) {
        HttpServletRequest httpServletRequest = RequestHolder.getRequest();
        String requestParamStr = null;
        try {
            requestParamStr = JSON.toJSONString(requestParam);
        } catch (Throwable e) {
            log.warn("unsupported data type");
        }
        MallPcLogVo mallPcLogVo = new MallPcLogVo().setClientIp(HttpUtil.getClientIP(httpServletRequest)).
                setRequestMethod(httpServletRequest.getRequestURI()).
                setRequestParam(requestParamStr).
                setExecuteTime(executeTime);
        if (Objects.nonNull(result)) {
            mallPcLogVo.setResponseCode(result.getCode()).
                    setResponseMsg(result.getMsg()).
                    setResponseData(result.isSuccess() ? JSON.toJSONString(result.getData()) : null).
                    setResponseExtraData(result.isSuccess() ? JSON.toJSONString(result.getExtra()) : null);
        }
        switch (mallPcLogVo.getRequestMethod()) {
            default:
                defaultLogOutput(mallPcLogVo);
                break;
        }
    }

    private void defaultLogOutput(MallPcLogVo mallPcLogVo) {
        log.info(StringUtils.join("	", mallPcLogVo.getClientIp(),
                "	", mallPcLogVo.getUserId(),
                "	", mallPcLogVo.getUserName(),
                "	", mallPcLogVo.getRequestMethod(),
                "	", mallPcLogVo.getExecuteTime() + "ms",
                "	", mallPcLogVo.getResponseCode(),
                "	", mallPcLogVo.getResponseMsg(),
                "	", mallPcLogVo.getRequestParam(),
                "	", mallPcLogVo.getResponseData(),
                "	", mallPcLogVo.getResponseExtraData()
                )
        );
    }

}
