package com.xingyun.bbc.mallpc.model.vo.pageconfig;

import java.io.Serializable;

public class ModuleVo implements  Serializable
{

	
	/**
	 * @字段：serialVersionUID
	 * @功能描述：
	 * @创建人：EDZ
	 * @创建时间：2019年9月3日下午8:52:52
	 */
	
	private static final long serialVersionUID = 1L;
	
    private Long fconfigId;

    private String fconfigName;

	public Long getFconfigId() {
		return fconfigId;
	}

	public void setFconfigId(Long fconfigId) {
		this.fconfigId = fconfigId;
	}

	public String getFconfigName() {
		return fconfigName;
	}

	public void setFconfigName(String fconfigName) {
		this.fconfigName = fconfigName;
	}
}
