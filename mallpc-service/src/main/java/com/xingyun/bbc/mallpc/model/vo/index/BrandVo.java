package com.xingyun.bbc.mallpc.model.vo.index;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author chenxiang
 * @ClassName: BrandVo
 * @Description:
 * @date 2019年11月18日 14:21:50
 */

@Data
public class BrandVo implements Serializable {

    private static final long serialVersionUID = 678048513794712391L;

    @ApiModelProperty(value = "商品品牌Id")
    private Long fbrandId;

    @ApiModelProperty(value = "商品品牌名称")
    private String fbrandName;

    @ApiModelProperty(value = "品牌首字母")
    private String fbrandInitail;

    @ApiModelProperty(value = "品牌logo图片地址")
    private String fbrandLogo;

    @ApiModelProperty(value = "品牌描述")
    private String fbrandDesc;

    @ApiModelProperty(value = "品牌排序")
    private Integer fbrandSort;

    @ApiModelProperty(value = "是否热门 0 否 1 是")
    private Integer fisHot;

    @ApiModelProperty(value = "品牌海报图片地址")
    private String fbrandPoster;

    @ApiModelProperty(value = "品牌国家")
    private String fcountryName;

//    @ApiModelProperty(value = "图片对象")
//    private ImageVo image;
//
//    @ApiModelProperty(value = "海报图片对象")
//    private ImageVo posterImage;
//
//    public void setFbrandLogo(String fbrandLogo){
//        this.fbrandLogo = fbrandLogo;
//        image = new ImageVo(fbrandLogo);
//    }
//
//    public void setFbrandPoster(String fbrandPoster){
//        this.fbrandPoster = fbrandPoster;
//        posterImage = new ImageVo(fbrandPoster);
//    }
}
