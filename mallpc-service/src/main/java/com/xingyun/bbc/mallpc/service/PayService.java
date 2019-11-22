package com.xingyun.bbc.mallpc.service;

import javax.servlet.http.HttpServletRequest;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.pay.model.dto.ThirdPayDto;


/**
 * @author jianghui
 * @Title:
 * @Description:
 * @date 2019-09-03 11:00
 */
public interface PayService {
	
	Result<?> createThirdPayUrl(ThirdPayDto dto,HttpServletRequest request);
	
	Result<?> createThirdPayCode(ThirdPayDto dto,HttpServletRequest request);
}
