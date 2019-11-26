package com.xingyun.bbc.mallpc.model.vo.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author nick
 * @version 1.0.0
 * @date 2019-11-18
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Data
public class SendSmsCodeVo {

    @ApiModelProperty("是否触发滑块验证 0否 1是")
    private Integer isCheck = 0;
}
