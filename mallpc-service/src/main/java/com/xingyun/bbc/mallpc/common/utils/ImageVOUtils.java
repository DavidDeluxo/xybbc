package com.xingyun.bbc.mallpc.common.utils;

import com.xingyun.bbc.core.utils.StringUtil;
import com.xingyun.bbc.mallpc.model.vo.ImageVo;

/**
 * @author pengaoluo
 * @version 1.0.0
 */
public class ImageVOUtils {
    public static ImageVo toImageVo(String rawUrl) {
        if (StringUtil.isEmpty(rawUrl)) {
            return null;
        }
        return new ImageVo(rawUrl);
    }
}
