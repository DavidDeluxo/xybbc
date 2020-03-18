package com.xingyun.bbc.mallpc.model.vo.shoppingcart;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lchm
 * @version 1.0.0
 * @date 2020/3/16 14:24
 * @description:
 * @package com.xingyun.bbc.mallpc.model.vo.shoppingcart
 */
@Data
@JsonInclude()
public class ImportDetailVo {

    @ApiModelProperty("商品编码")
    private String fskuCode;

    @ApiModelProperty("效期")
    private String fqualityDate;

    @ApiModelProperty("包装规格(件装)")
    private String fbatchPackageNum;

    @ApiModelProperty("数量")
    private String fskuNum;

    @ApiModelProperty("收件人姓名")
    private String fdeliveryName;

    @ApiModelProperty("收件人联系方式")
    private String fdeliveryMobile;

    @ApiModelProperty("省")
    private String fdeliveryProvinceName;

    @ApiModelProperty("市")
    private String fdeliveryCityName;

    @ApiModelProperty("区")
    private String fdeliveryAreaName;

    @ApiModelProperty("详细地址")
    private String fdeliveryAddr;

    @ApiModelProperty("身份证号码")
    private String fdeliveryCardid;

    @ApiModelProperty("身份证正面")
    private String fdeliveryCardUrlFront;

    @ApiModelProperty("身份证反面")
    private String fdeliveryCardUrlBack;

    @ApiModelProperty("商家平台名称")
    private String fplatformName;

    @ApiModelProperty("商家平台单号")
    private String fplatformOrderNo;

    /*************************************************/

    @ApiModelProperty("错误提示")
    private String errorMsg;
}