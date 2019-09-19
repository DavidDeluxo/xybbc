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
		ALIPAY_RECHARGE(1, "支付宝充值"),
		WEIXIN_RECHARGE(2, "微信充值"),
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
