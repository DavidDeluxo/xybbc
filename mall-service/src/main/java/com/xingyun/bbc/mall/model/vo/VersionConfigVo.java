package com.xingyun.bbc.mall.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author chenxiang
 * @version 1.0.0
 * @date 2019/12/23
 */
@Data
public class VersionConfigVo implements Serializable {

    private static final long serialVersionUID = 6681660495924052033L;
    /**
     * 最低历史版本
     */
    private String minVersion;

    /**
     * 更新类型：1：强制
     */
    private Integer type;

}
