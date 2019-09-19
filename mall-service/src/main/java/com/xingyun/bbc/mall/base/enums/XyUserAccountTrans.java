/**   
* @Title: XyUserAccountTrans.java 
* @Package com.xingyun.xyb2b.common.enumconfig 
* @Description: TODO
* @author Tito
* @date 2017年6月8日 下午5:58:26 
* @company 版权所有 深圳市天行云供应链有限公司
* @version V1.0   
*/
package com.xingyun.bbc.mall.base.enums;

/** 
* @ClassName: XyUserAccountTrans 
* @Description: TODO
* @author Tito
* @date 2017年6月8日 下午5:58:26 
*  
*/
public class XyUserAccountTrans {
	
	public static enum UserAccountTransThdPayTypeEnum {
		ALIPAY_RECHARGE(1, "支付宝"),
		WEIXIN_RECHARGE(2, "微信支付"),
		HUIFU_BANK_RECHARGE(3, "汇付天下"),
		REITTANCE_RECHARGE(4, "线下汇款"),	
		;

		private final int value;

		private final String name;

		UserAccountTransThdPayTypeEnum(int value, String name) {
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
			for (UserAccountTransThdPayTypeEnum em : UserAccountTransThdPayTypeEnum.values()) {
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
			for (UserAccountTransThdPayTypeEnum em : UserAccountTransThdPayTypeEnum.values()) {
				if (em.getValue() == v) {
					return em.getName();
				}
			}
			return null;
		}
	}
}
