package com.xingyun.bbc.mallpc.model.dto.shoppingcart;

import com.xingyun.bbc.mallpc.model.dto.BaseDto;
import com.xingyun.bbc.mallpc.model.validation.ShoppingCartValidator;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-11-19
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Data
@Accessors(chain = true)
public class ShoppingCartDto extends BaseDto {

    private static final long serialVersionUID = -3237379499351876387L;

    /**
     * sku编码
     */
    @NotNull(message = "sku编码不能为空", groups = {ShoppingCartValidator.Add.class})
    private String fskuId;

    /**
     * 批次号
     */
    @NotBlank(message = "批次号不能为空", groups = {ShoppingCartValidator.Add.class})
    private String fbatchId;

    /**
     * 批次包装规格ID
     */
    @NotNull(message = "批次包装规格ID不能为空", groups = {ShoppingCartValidator.Add.class})
    private Long fbatchPackageId;

    /**
     * 购买数量
     */
    @NotNull(message = "购买数量不能为空", groups = {ShoppingCartValidator.Add.class, ShoppingCartValidator.EditNum.class})
    @Min(value = 1, message = "购买数量不能小于1", groups = {ShoppingCartValidator.Add.class, ShoppingCartValidator.EditNum.class})
    private Integer fbuyNum;

    /**
     * 进货单商品ID列表
     */
    @NotEmpty(message = "进货单商品ID不能为空", groups = {ShoppingCartValidator.Checkout.class})
    private List<Long> ids;

    /**
     * 是否清空失效商品
     */
    private Boolean clearInvalidGoods;

}
