package com.xingyun.bbc.mall.base.utils;

import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.utils.StringUtil;
import com.xingyun.bbc.mall.common.enums.MallResultStatus;

import javax.servlet.http.HttpServletRequest;

/**
 * @author hekaijin
 * @Description:
 * @createTime: 2019-09-10 17:45
 */
public class UserUtil {

    public static Long uid(HttpServletRequest request) {

        String xyId = request.getHeader("xyId");

        if (StringUtil.isEmpty(xyId)) throw new BizException(MallResultStatus.NOT_LOGIN);

        long uid = -1;
        try {

            uid = Long.parseLong(xyId);
        }catch (NumberFormatException e){
            throw new BizException(MallResultStatus.NOT_LOGIN);
        }

        if (uid ==-1) throw new BizException(MallResultStatus.NOT_LOGIN);

        return uid;

    }
}
