package com.xingyun.bbc.mallpc.model.vo.pay;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lchm
 * @version 1.0.0
 * @date 2020/3/3 13:46
 * @description: TODO
 * @package com.xingyun.bbc.mallpc.model.vo.pay
 */
@Data
public class OrderExpressExportVo {

    @Excel(name = "物流公司", width = 20, needMerge = true)
    @ApiModelProperty(value = "物流公司", dataType = "string")
    private String fcompanyName;

    @Excel(name = "快递单号", width = 20, needMerge = true)
    @ApiModelProperty(value = "快递单号", dataType = "string")
    private String fexpressBillNo;

}