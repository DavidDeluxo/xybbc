package com.xingyun.bbc.mall.common.constans;

import java.math.BigDecimal;

public class PageConfigContants {

	public  static  final  String  PAGE_CONFIG="PAGE_CONFIG";//redis hash key 业务前缀 table表名
	
	public  static  final  String  MODULE_POSITION_ROWID="MODULE_{POSITION}_{ROWID}";//导航栏位置与ROW ID

	/**
	 * BigDecimal(10000)
	 */
	public static final BigDecimal BIG_DECIMAL_10000 = new BigDecimal(10000);

	/**
	 * BigDecimal(10000)
	 */
	public static final BigDecimal BIG_DECIMAL_100 = new BigDecimal(100);

}
