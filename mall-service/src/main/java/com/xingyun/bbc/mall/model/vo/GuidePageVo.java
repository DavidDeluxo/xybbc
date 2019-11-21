package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;
/**
 * @author:feixiaojie
 */
public class GuidePageVo implements Serializable{

    private static final long serialVersionUID = 1L;
	
    /** 主键id */
    @ApiModelProperty("引导id")
    private Long fguideId;
    
    @ApiModelProperty("针对配置对象 0 app配置 1 PC配置 默认0")
    private Long fguideType;

    /** 图片地址 */
    @ApiModelProperty("图片地址")
    private String fimgUrl;

    /** 是否删除:0未删除 1已删除 */
    @ApiModelProperty("是否删除:0未删除 1已删除")
    private Integer fisDelete;
    /**类型:0引导页配置1启动页配置**/
    @ApiModelProperty("类型:0引导页配置1启动页配置")
    private Integer ftype;

    /** 创建时间 */
    @ApiModelProperty("创建时间")
    private Date fcreateTime;

    /** 更新时间 */
    @ApiModelProperty("更新时间")
    private Date fmodifyTime;


    public void setFguideId(Long fguideId) {
        this.fguideId = fguideId;
    }

    public Long getFguideId() {
        return this.fguideId;
    }
    public Long getFguideType() {
      return fguideType;
    }

    public void setFguideType(Long fguideType) {
      this.fguideType = fguideType;
    }

    public void setFimgUrl(String fimgUrl) {
        this.fimgUrl = fimgUrl;
    }

    public String getFimgUrl() {
        return this.fimgUrl;
    }
    public void setFisDelete(Integer fisDelete) {
        this.fisDelete = fisDelete;
    }

    public Integer getFisDelete() {
        return this.fisDelete;
    }
    public void setFcreateTime(Date fcreateTime) {
        this.fcreateTime = fcreateTime;
    }

    public Date getFcreateTime() {
        return this.fcreateTime;
    }
    public void setFmodifyTime(Date fmodifyTime) {
        this.fmodifyTime = fmodifyTime;
    }

    public Date getFmodifyTime() {
        return this.fmodifyTime;
    }

	public Integer getFtype() {
		return ftype;
	}

	public void setFtype(Integer ftype) {
		this.ftype = ftype;
	}
}