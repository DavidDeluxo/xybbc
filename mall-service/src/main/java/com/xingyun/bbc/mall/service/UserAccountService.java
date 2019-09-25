package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.dto.UserRechargeDto;
import com.xingyun.bbc.mall.model.vo.UserRechargeVo;

/**
 * @author jianghui
 * @Title:
 * @Description:
 * @date 2019-09-03 11:00
 */
public interface UserAccountService {
	
	/**
	 * @author jianghui
	 * @version V1.0
	 * @Description: 用户充值
	 * @Param: [UserRechargeDto]
	 * @return: com.xingyun.bbc.core.utils.Result<com.xingyun.bbc.mall.model.vo.UserRechargeVo>
	 * @date 2019/8/20 13:49
	 */
	Result<UserRechargeVo> insertUserBalance(UserRechargeDto userRechargeDto);
}
