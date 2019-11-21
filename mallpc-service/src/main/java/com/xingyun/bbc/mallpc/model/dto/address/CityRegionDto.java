package com.xingyun.bbc.mallpc.model.dto.address;

import com.xingyun.bbc.mallpc.model.validation.extensions.annotations.NumberRange;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author nick
 * @version 1.0.0
 * @date 2019-11-18
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Data
public class CityRegionDto {

    @ApiModelProperty("父类目ID")
    @NotNull(message = "父级id不能为空")
    private Integer fpRegionId;

    @ApiModelProperty("区域类型，1为国家；2为省/直辖市；3为地级市；4为区/县")
    @NumberRange(values = {1,2,3,4},message = "区域类型值非法")
    @NotNull(message = "区域类型不能为空")
    private Integer fRegionType;

}
