package com.xingyun.bbc.mallpc.model.vo.shoppingcart;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lchm
 * @version 1.0.0
 * @date 2020/3/16 10:26
 * @description: TODO
 * @package com.xingyun.bbc.mallpc.model.vo.shoppingcart
 */
@Data
public class ImportShoppingCartExcelVo {

    @ApiModelProperty("正确订单数")
    private Integer correctCount;

    @ApiModelProperty("错误订单数")
    private Integer errorCount;

    @ApiModelProperty("临时存储id")
    private String temporaryNo;

    @ApiModelProperty("进货单列表")
    private List<ImportDetailVo> detailVoList;
}