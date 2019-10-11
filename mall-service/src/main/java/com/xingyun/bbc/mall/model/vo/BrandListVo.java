package com.xingyun.bbc.mall.model.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import java.util.Date;

@Data
@ApiModel("商品品牌")
public class BrandListVo implements Comparable<BrandListVo>{

    @ApiModelProperty(value = "品牌id")
    private Long fbrandId;

    @ApiModelProperty(value = "品牌名称")
    private String fbrandName;

    @ApiModelProperty(value = "品牌logo图片地址")
    private String fbrandLogo;

    @ApiModelProperty(value = "品牌排序")
    private Integer fbrandSort;

    @ApiModelProperty(value = "创建时间")
    private Date fcreateTime;

    @Override
    public int compareTo(BrandListVo compare) {
        int i = this.getFbrandSort().compareTo(compare.getFbrandSort());
        if (i == 0) {
            i = this.getFcreateTime().compareTo(compare.fcreateTime);
        }
        return i;
    }
}
