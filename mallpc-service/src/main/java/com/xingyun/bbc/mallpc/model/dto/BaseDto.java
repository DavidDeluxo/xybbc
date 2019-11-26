package com.xingyun.bbc.mallpc.model.dto;

import com.xingyun.bbc.mallpc.model.validation.ShoppingCartValidator;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-08-24
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Data
@Accessors(chain = true)
public class BaseDto implements Serializable {


    private static final long serialVersionUID = 2766079010834372301L;

    /**
     * 数据主键
     */
    @NotNull(message = "ID不能为空", groups = {ShoppingCartValidator.EditNum.class})
    private Long id;

    /**
     * 查询类型
     */
    private Integer searchType;

    /**
     * 查询关键字
     */
    private String keyword;


}
