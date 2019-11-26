package com.xingyun.bbc.mallpc.model.vo.address;

import io.swagger.annotations.ApiParam;
import lombok.Data;

import java.util.List;

/**
 * @author pengaoluo
 * @version 1.0.0
 * @date 2019/8/26
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Data
public class CityRegionAllVO {

    @ApiParam("城市区域ID")
    private Integer value;

    @ApiParam("城市区域名称")
    private String label;

    @ApiParam("下属区域")
    private List<CityRegionAllVO> children;

}
