package com.xingyun.bbc.mallpc.model.dto.aftersale;

import com.xingyun.bbc.mallpc.model.dto.PageDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel(value = "售后列表dto")
public class AftersalePcLisDto extends PageDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户id")
    private Long fuid;

    @ApiModelProperty(value = "订单号")
    private String forderId;

    @ApiModelProperty(value = "售后单号")
    private String forderAftersaleId;

    @ApiModelProperty(value = "商品名称")
    private String fskuName;

    @ApiModelProperty(value = "下单时间起始")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date forderTimeStart;

    @ApiModelProperty(value = "下单时间结尾")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date forderTimeEnd;

}
