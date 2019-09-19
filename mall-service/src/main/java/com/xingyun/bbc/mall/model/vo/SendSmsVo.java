package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author ZSY
 * @Description: 短信发送返回
 * @createTime: 2019-09-03 11:30
 */
@Data
public class SendSmsVo {
    @ApiModelProperty("验证码key")
    private String authNumKey;

    @ApiModelProperty("是否触发滑块验证 0否 1是")
    private Integer isCheck;

}
