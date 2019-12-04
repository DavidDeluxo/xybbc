package com.xingyun.bbc.mall.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xingyun.bbc.common.redis.XyIdGenerator;
import com.xingyun.bbc.common.redis.order.OrderTypeEnum;
import com.xingyun.bbc.common.redis.order.RechargeOrderBizEnum;
import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.user.api.UserAccountTransApi;
import com.xingyun.bbc.core.user.api.UserAccountTransWaterApi;
import com.xingyun.bbc.core.user.po.UserAccountTrans;
import com.xingyun.bbc.core.user.po.UserAccountTransWater;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.enums.XyUserAccountTrans.UserAccountTransThdPayTypeEnum;
import com.xingyun.bbc.mall.base.utils.DozerHolder;
import com.xingyun.bbc.mall.base.utils.PriceUtil;
import com.xingyun.bbc.mall.model.dto.UserRechargeDto;
import com.xingyun.bbc.mall.model.vo.UserRechargeVo;
import com.xingyun.bbc.mall.service.UserAccountService;

import io.seata.spring.annotation.GlobalTransactional;


/**
 * @author ZSY
 * @Title:
 * @Description:
 * @date 2019-09-03 11:00
 */
@Service
public class UserAccountServiceImpl implements UserAccountService {
    private static Logger logger = LoggerFactory.getLogger(UserAccountServiceImpl.class);
    
    
	@Autowired
	private UserAccountTransApi userAccountTransApi;
	
	@Autowired
	private UserAccountTransWaterApi userAccountTransWaterApi;
	
	@Autowired
	private DozerHolder dozerHolder;
    
    
	/**
	 * @author jianghui
	 * @version V1.0
	 * @Description: 用户充值
	 * @Param: [UserRechargeDto]
	 * @return: com.xingyun.bbc.core.utils.Result<com.xingyun.bbc.mall.model.vo.UserRechargeVo>
	 * @date 2019/8/20 13:49
	 */
    @Override
    @GlobalTransactional
    public Result<UserRechargeVo> insertUserBalance(UserRechargeDto userRechargeDto) {
        //将充值金额转换为分
    	Long recharge = PriceUtil.toPenny(userRechargeDto.getFrecharger()).longValue();
        //生成充值单号
        String transOrderId = XyIdGenerator.generateId(OrderTypeEnum.RECHARGE_ORDER.getCode(),RechargeOrderBizEnum.TRADE.getCode());
        //需要向交易明细表中插入数据
        UserAccountTrans userAccountTrans = new UserAccountTrans();
        userAccountTrans.setFtransId(transOrderId);
        userAccountTrans.setFtransAmount(recharge);
        userAccountTrans.setFuid(userRechargeDto.getFuid());
        userAccountTrans.setFtransTypes(1);//设置类型为充值
        if(userRechargeDto.getFrechargeType()==UserAccountTransThdPayTypeEnum.REITTANCE_RECHARGE.getValue())//线下汇款充值
        {
        	userAccountTrans.setFtransReason("线下汇款");//充值理由
        }
        userAccountTrans.setFtransStatus(1);//设置充值状态未付款
        userAccountTrans.setFrechargeType(userRechargeDto.getFrechargeType());//支付类型
        Result<Integer> result = userAccountTransApi.create(userAccountTrans);//将数据插入交易明细表
        logger.info("生成第三方充值订单：" + transOrderId + "金额：" + recharge + "(单位，分)");
        if(result.isSuccess())
        {
            //将数据插入订单流水表
            UserAccountTransWater userAccountTransWater = dozerHolder.convert(userAccountTrans, UserAccountTransWater.class);
            userAccountTransWaterApi.create(userAccountTransWater);
        }else {
        	logger.info("用户充值失败");
			throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
		}
        //返回一个带交易单号和充值金额的
        UserRechargeVo userRechargeVo = new UserRechargeVo();
        userRechargeVo.setFtransId(transOrderId);
        userRechargeVo.setFrecharger(PriceUtil.toYuan(recharge));
        return Result.success(userRechargeVo);
    }
    

}
