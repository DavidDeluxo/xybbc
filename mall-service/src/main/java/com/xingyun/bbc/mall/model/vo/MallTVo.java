package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("商城App通用传参模型")
public class MallTVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Object tId;

    private String tName;

    private Integer tNum;
}
