package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import java.io.Serializable;

/**
 * @author hekaijin
 * @date 2019/9/19 13:55
 * @Description
 */
@ApiModel("开户行列表")
@Data
@Accessors(chain = true)
public class BanksVo implements Serializable {

    private static final long serialVersionUID = 1670568260369371059L;

    @ApiModelProperty("开户行图标")
    private String icon;

    @ApiModelProperty("开户行code")
    private String bankCode;

    @ApiModelProperty("开户行名称")
    private String bankName;
}
