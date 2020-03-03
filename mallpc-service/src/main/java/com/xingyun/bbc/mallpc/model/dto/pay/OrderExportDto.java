package com.xingyun.bbc.mallpc.model.dto.pay;

import com.xingyun.bbc.mallpc.model.dto.PageDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lchm
 * @version 1.0.0
 * @date 2020/3/3 14:15
 * @description: TODO
 * @package com.xingyun.bbc.mallpc.model.dto.pay
 */
@Data
public class OrderExportDto extends PageDto {

    @ApiModelProperty(hidden = true)
    private Long fuid;

    @ApiModelProperty(value = "订单状态(不填时查询全部)")
    private Integer forderStatus;

    @ApiModelProperty(value = "每页条数(选填 不传默认取10)")
    private Integer pageSize;

    @ApiModelProperty(value = "支付单号")
    private String forderPaymentId;

    @ApiModelProperty(value = "销售单号")
    private String forderId;

    @ApiModelProperty(value = "收货人姓名")
    private String fdeliveryName;

    @ApiModelProperty(value = "收货人手机")
    private String fdeliveryMobile;

    @ApiModelProperty(value = "sku名称")
    private String fskuName;

    @ApiModelProperty(value = "sku编号")
    private String fskuCode;

    @ApiModelProperty(value = "下单时间起始")
    private String forderTimeStart;

    @ApiModelProperty(value = "下单时间结尾")
    private String forderTimeEnd;

    @ApiModelProperty(value = "支付订单号集合")
    private List<String> orderPaymentIdList;

    @ApiModelProperty(value = "销售订单号集合")
    private List<String> orderIdList;
}