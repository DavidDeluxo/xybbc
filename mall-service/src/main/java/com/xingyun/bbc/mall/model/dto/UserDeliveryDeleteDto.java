package com.xingyun.bbc.mall.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author:lll
 */
@Data
public class UserDeliveryDeleteDto implements Serializable{



    /** 用户收货地址IDS */
    @ApiModelProperty("用户IDS,批量删除需要用，拼接成字符串")
    private String fdeliveryUserIds;
}