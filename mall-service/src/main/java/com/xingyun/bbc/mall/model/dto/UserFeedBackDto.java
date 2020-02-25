package com.xingyun.bbc.mall.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("用户问题反馈dto")
public class UserFeedBackDto implements Serializable {

    /** 用户id */
    @ApiModelProperty("用户id")
    private Long fuid;

    /** 问题类型（1.功能异常 2.体验问题 3.新功能建议 4.没有我想要的商品 5.其他反馈） */
    @ApiModelProperty("问题类型（1.功能异常 2.体验问题 3.新功能建议 4.没有我想要的商品 5.其他反馈）")
    @NotNull(message = "问题类型不能为空")
    private Integer ffeedbackType;

    /** 问题描述 */
    @ApiModelProperty("问题描述")
    @NotBlank(message = "问题描述不能为空")
    private String ffeedbackDetail;

    /** 问题反馈图片（多张逗号分隔） */
    @ApiModelProperty("问题反馈图片（多张逗号分隔）")
    private String[] ffeedbackPiclis;

    /** 用户联系方式 */
    @ApiModelProperty("用户联系方式")
    private String fuserTel;

}
