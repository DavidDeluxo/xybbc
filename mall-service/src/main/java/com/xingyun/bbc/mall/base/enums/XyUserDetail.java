package com.xingyun.bbc.mall.base.enums;

/**
 * UserDetail相关字段枚举
* @ClassName: XyUserDetail 
* @Description: 用户账户详情枚举配置
* @author sup
* @date 2016年11月25日 下午5:54:04 
*
 */
public class XyUserDetail {

	public static enum UserDetailTypeEnum {
		ALI_RECHARGE(1, "用户支付宝充值"),
		WECHAT_RECHARGE(2, "用户微信充值"),
		HF_RECHARGE(3, "用户汇付充值"),
		OFFLINE_RECHARGE(4, "用户线下汇款充值"),
		BALANCE_ORDER(5, "余额下单"),
		ALI_ORDER(6, "支付宝下单"),
		WECHAT_ORDER(7, "微信下单"),
		BALANCE_WITHDRAW(8, "余额提现"),
		CUSTOM_CANCEL(9, "客服取消订单"),
		AFTERSALE_BALANCE(10, "售后退款至余额"),
		AUTO_CANCEL_BALANCE(11, "自动取消订单退款至余额"),
		MANUAL_CANCEL_BALANCE(12, "手动取消订单退款至余额"),
		AFTERSALE_WORK_BALANCE(13, "售后工单调整信用额度"),
		AFTERSALE_WORK_CREDIT(14, "账户调整单调整客户账户余额"),
		USER_WORK_CREDIT(15, "支付宝下单"),
		BUYAGENT_INCOME(16, "代购收益");
		;
		
		
		
		private final int value;

		private final String name;

		UserDetailTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}
		
		public static String getName(int value) {
			for (UserDetailTypeEnum em : UserDetailTypeEnum.values()) {
				if (em.getValue() == value) {
					return em.getName();
				}
			}
			return null;
		}
		
		public static String getName(Object value) {
			if (value == null) {
				return null;
			}
			int v = Integer.parseInt(String.valueOf(value));
			for (UserDetailTypeEnum em : UserDetailTypeEnum.values()) {
				if (em.getValue() == v) {
					return em.getName();
				}
			}
			return null;
		}
	}
}
