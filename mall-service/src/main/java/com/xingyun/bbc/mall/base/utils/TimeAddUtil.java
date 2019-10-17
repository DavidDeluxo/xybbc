package com.xingyun.bbc.mall.base.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Thstone
 * @version V1.0
 * @Title:
 * @Package com.xingyun.bbc.order.base.util
 * @Description: (用一句话描述该文件做什么)
 * @date 2019/9/9 21:36
 */
public class TimeAddUtil extends com.xingyun.bbc.core.utils.DateUtil {

	public static Date addMinute(Date date, int hourAmount) {
		return add(date, 12, hourAmount);
	}

	private static Date add(Date date, int calendarField, int amount) {
		if (date == null) {
			return null;
		} else {
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			c.add(calendarField, amount);
			return c.getTime();
		}
	}
}
