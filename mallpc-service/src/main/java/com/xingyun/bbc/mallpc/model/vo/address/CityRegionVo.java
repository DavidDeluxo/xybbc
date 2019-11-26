package com.xingyun.bbc.mallpc.model.vo.address;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author nick
 * @version 1.0.0
 * @date 2019-11-18
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Data
public class CityRegionVo implements Serializable {

    private static final long serialVersionUID = 1679498790454536281L;

    @ApiModelProperty(value = "城市区域ID")
    private Integer fregionId;

    @ApiModelProperty(value = "父类目ID")
    private Integer fpRegionId;

    @ApiModelProperty(value = "城市区域名称")
    private String fcrName;
}
