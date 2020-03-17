package com.xingyun.bbc.mallpc.model.dto.subject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author lchm
 * @version 1.0.0
 * @date 2020/1/13 10:15
 * @package com.xingyun.bbc.mall.model.dto
 */
@Data
@ApiModel("专题查询")
public class SubjectQueryDto {

    {
        this.pageIndex = 1;
        this.pageSize = 10;
        this.isLogin = false;
    }

    /**
     * 专题id
     */
    @ApiModelProperty("专题id")
    @NotNull(message = "专题id不能为空")
    private Long fsubjectId;

    @ApiModelProperty("页大小")
    private Integer pageSize;

    @ApiModelProperty("页码")
    private Integer pageIndex;

    @ApiModelProperty(value = "用户是否登录", hidden = true)
    private Boolean isLogin;

    @ApiModelProperty(value = "认证类型：1实体门店，2网络店铺，3网络平台，4批采企业，5微商代购", hidden = true)
    private Integer foperateType;

    @ApiModelProperty(value="用户Id", hidden=true)
    private Integer fuid;

    @ApiModelProperty(value = "用户类型", hidden = true)
    private String fuserTypeId;

    @ApiModelProperty("优惠券ID")
    private Long couponId;
}