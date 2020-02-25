package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("用户问题反馈vo")
public class UserFeedBackVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("问题类型（1.功能异常 2.体验问题 3.新功能建议 4.没有我想要的商品 5.其他反馈）")
    private Integer ffeedbackType;

    @ApiModelProperty("问题类型（1.功能异常 2.体验问题 3.新功能建议 4.没有我想要的商品 5.其他反馈）")
    private String ffeedbackTypeStr;

    @ApiModelProperty("问题类型描述")
    private String ffeedbackTypeInfor;

}
