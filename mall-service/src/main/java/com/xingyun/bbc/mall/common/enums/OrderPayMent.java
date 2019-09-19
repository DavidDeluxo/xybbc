package com.xingyun.bbc.mall.common.enums;

public class OrderPayMent {
	
	public static enum PayTypeEnum {
		BALANCE_PAY(1, "余额"),
		ALI_PAY(2, "支付宝"),
		WECHAT_PAY(3, "微信"),
		CREDIT_PAY(4, "信用"),
		MIX_PAY(5, "混合支付");
		;

		private final int value;

		private final String name;

		PayTypeEnum(int value, String name) {
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
			for (PayTypeEnum em : PayTypeEnum.values()) {
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
			for (PayTypeEnum em : PayTypeEnum.values()) {
				if (em.getValue() == v) {
					return em.getName();
				}
			}
			return null;
		}
	}
	
	
	public static enum OrderPayEnum {
		BALANCE_PAY(1), //余额
		ALI_PAY(2),//支付宝
		WECHAT_PAY(3),//微信
		CREDIT_PAY(4),//信用
		MIX_PAY(5);//混合支付
		;
		// 支付方式 1余额,2支付宝,3微信,4信用,5混合支付

		private int value;

		private OrderPayEnum(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}
	
	public static enum OrderPayStatusEnum {
		WAITING_PAY(1),   	//待付款
        TO_BE_DELIVERED(2),  //待发货
        PENDING_RECEIPT(3), //待收货
        RECEIVED(4), //已收货
        DONE(5), //已完成
        CLOSE(6), //已关闭
		;
		//订单状态 1待付款、2待发货、3待收货、 4已收货 、5已完成、 6已关闭
		
		private int value;

		private OrderPayStatusEnum(int value){
			this.value=value;
		}
		
		public int getValue() {
			return value;
		}
	}
	
	public static enum OrderThirdTradeStatusEnum {
		WAITING_PAY(1),   	//处理中
		SUCCESS_PAY(2),  //成功支付
		FAIL_PAY(3), //订单取消
		EXCEPTION_PAY(4)  //支付异常
		; 
		//'第三方支付状态 1处理中 2成功 3失败 4异常'
		
		private int value;

		private OrderThirdTradeStatusEnum(int value){
			this.value=value;
		}
		
		public int getValue() {
			return value;
		}
	}

	public static enum ThirdOrderPayTypeEnum{

		ALI_PAY(3,1,"支付宝支付"),   	//支付宝支付
		WEIXIN_PAY(6,2,"微信支付"),  //微信支付
		BALANCE_PAY(1,6,"余额支付"),
		CREDIT_PAY(2,7,"信用支付"),
		MIX_PAY(5,8,"混合支付");//混合支付

		//'第三方支付状态 1处理中 2成功 3失败 4异常'

		private int pay_type;

		private int third_pay_type;

		private String desc;

		private ThirdOrderPayTypeEnum(int pay_type,int third_pay_type,String desc){
			this.pay_type = pay_type;
			this.third_pay_type = third_pay_type;
			this.desc = desc;
		}

		public int getPay_type() {
			return pay_type;
		}

		public void setPay_type(int pay_type) {
			this.pay_type = pay_type;
		}

		public int getThird_pay_type() {
			return third_pay_type;
		}

		public void setThird_pay_type(int third_pay_type) {
			this.third_pay_type = third_pay_type;
		}

		public String getDesc() {
			return desc;
		}

		public void setDesc(String desc) {
			this.desc = desc;
		}

		public static ThirdOrderPayTypeEnum getByPayType(Integer pay_type) {
			if (pay_type == null) {
				return null;
			}
			for (ThirdOrderPayTypeEnum em : ThirdOrderPayTypeEnum.values()) {
				if (em.pay_type == pay_type.intValue()) {
					return em;
				}
			}
			return null;
		}

	}
}
