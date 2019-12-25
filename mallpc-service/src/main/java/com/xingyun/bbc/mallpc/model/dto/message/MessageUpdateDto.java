package com.xingyun.bbc.mallpc.model.dto.message;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Description 消息更新对象
 * @ClassName MessageUpdateDto
 * @Author ming.yiFei
 * @Date 2019/12/20 17:48
 **/
@Data
public class MessageUpdateDto {

    @ApiModelProperty(value = "消息中心-消息类型")
    private Integer messageCenterType;

    /**
     * 用户id
     */
    private Long userId;
}
