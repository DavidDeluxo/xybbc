/**   
* @Title: RechargeService.java 
* @Package com.xingyun.xyb2b.pay.service 
* @Description: TODO
* @author Tito
* @date 2017年7月17日 上午10:04:25 
* @company 版权所有 深圳市天行云供应链有限公司
* @version V1.0   
*/
package com.xingyun.bbc.mall.service;

import java.util.Map;

import com.xingyun.bbc.pay.model.vo.PayInfoVo;




/** 
* @ClassName: RechargeService 
* @Description: TODO
* @author Tito
* @date 2017年7月17日 上午10:04:25 
*  
*/
public interface RechargeService {

	
	int newUpdateAfterRechargeSuccess(PayInfoVo thirdPayInfo);

	
	/**
	* @Title: updateAfterRechargeSuccess 
	* @Description: 第三方支付成功后，修改账户信息 
	* @param thirdPayInfo	第三方支付信息
	* @return 0-失败，1-成功
	* @author Tito
	 */
	int updateAfterRechargeSuccess(Map<String, String> thirdPayInfo);
}
