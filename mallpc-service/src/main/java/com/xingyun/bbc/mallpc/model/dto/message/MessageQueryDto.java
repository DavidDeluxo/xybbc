package com.xingyun.bbc.mallpc.model.dto.message;

import com.xingyun.bbc.mallpc.model.dto.PageDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Description 消息查询对象
 * @ClassName MessageQueryDto
 * @Author ming.yiFei
 * @Date 2019/12/20 16:27
 **/
@Data
public class MessageQueryDto extends PageDto {

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "消息id")
    private Long messageId;

    @ApiModelProperty(value = "消息中心-消息类型")
    private Integer messageCenterType;

}
