package com.xingyun.bbc.mallpc.model.dto.shoppingcart;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @author lchm
 * @version 1.0.0
 * @date 2020/3/16 10:17
 * @description: TODO
 * @package com.xingyun.bbc.mallpc.model.dto.shoppingcart
 */
@Data
@ExcelTarget("ImportShoppingCart")
public class ImportShoppingCartExcelDto {

    @Excel(name = "商品编码")
    @NotEmpty(message = "商品编码不能为空")
    private String fskuCode;

    @Excel(name = "效期", databaseFormat = "yyyy-MM")
    private String fqualityDate;

    @Excel(name = "包装规格(件装)")
    @NotEmpty(message = "包装规格不能为空")
    private String fbatchPackageNum;

    @Excel(name = "数量")
    @NotEmpty(message = "数量不能为空")
    private String fskuNum;

    @Excel(name = "收件人姓名")
    @NotEmpty(message = "收件人姓名不能为空")
    private String fdeliveryName;

    @Excel(name = "收件人联系方式")
    @NotEmpty(message = "收件人联系方式不能为空")
    private String fdeliveryMobile;

    @Excel(name = "省")
    @NotEmpty(message = "省不能为空")
    private String fdeliveryProvinceName;

    @Excel(name = "市")
    @NotEmpty(message = "市不能为空")
    private String fdeliveryCityName;

    @Excel(name = "区")
    @NotEmpty(message = "区不能为空")
    private String fdeliveryAreaName;

    @Excel(name = "详细地址")
    @NotEmpty(message = "详细地址不能为空")
    private String fdeliveryAddr;

    @Excel(name = "身份证号码")
    private String fdeliveryCardid;

//    @Excel(name = "身份证正面", type = 2)
    @Excel(name = "身份证正面", type = 2, savePath = "D:\\excel\\upload\\img")
    private String fdeliveryCardUrlFront;

//    @Excel(name = "身份证反面", type = 2)
    @Excel(name = "身份证反面", type = 2, savePath = "D:\\excel\\upload\\img")
    private String fdeliveryCardUrlBack;

    @Excel(name = "商家平台名称")
    @NotEmpty(message = "商家平台名称不能为空")
    private String fplatformName;

    @Excel(name = "商家平台单号")
    @NotEmpty(message = "商家平台单号不能为空")
    private String fplatformOrderNo;
}