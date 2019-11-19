package com.xingyun.bbc.mallpc.model.vo.detail;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
@ApiModel("商品规格扩展模型")
public class GoodspecificationExVo  implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "规格标题")
    private String title;

    @ApiModelProperty(value = "后台传参Id类型 后台接收key值")
    private String keyType;

    @ApiModelProperty(value = "后台传参Id数据类型 1传数字 2传字符串")
    private Integer idType;

    @ApiModelProperty(value = "通用key-value传参")
    private List<MallTVo> item;


}
