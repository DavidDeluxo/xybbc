package com.xingyun.bbc.mallpc.model.dto.sku;

import com.xingyun.bbc.mallpc.model.dto.PageDto;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author lchm
 * @version 1.0.0
 * @date 2020/3/4 13:46
 * @description: TODO
 * @package com.xingyun.bbc.mallpc.model.dto.sku
 */
@Data
@AllArgsConstructor
public class SaleSkuExportDto extends PageDto {

    private Integer foperateType;

}