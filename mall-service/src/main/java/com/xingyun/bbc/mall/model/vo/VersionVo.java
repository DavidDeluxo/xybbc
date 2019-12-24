package com.xingyun.bbc.mall.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author chenxiang
 * @version 1.0.0
 * @date 2019/12/23
 */
@Data
public class VersionVo implements Serializable {

    private static final long serialVersionUID = -9027509404514878222L;
    /**
     * 版本id
     */
    private Integer fappVersionId;

    /**
     * 版本号
     */
    private String fversionNo;

    /**
     * 平台 1：IOS，2：安卓
     */
    private Integer fplatform;

    /**
     * 更新类型：1：非强制，2：强制，3：静默
     */
    private Integer fupdateType;

    /**
     * 更新说明
     */
    private String fcontent;

    /**
     * 更新条件 1：全部，2：指定，3：排除
     */
    private Integer fcondition;

    /**
     * 更新条件关联的版本主键，英文逗号分隔
     */
    private List<String> fVersionNos;

    /**
     * 最低历史版本
     */
    private String minVersion;

    /**
     * 最低历史版本是否强制更新，1强制更新
     */
    private Integer minVersionUpdateType;
}