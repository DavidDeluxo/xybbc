package com.xingyun.bbc.mall.base.utils;


import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;

import com.xingyun.bbc.mall.common.constans.PayConstants;

import java.util.Date;

/**
 * 日期工具类
 *
 * @author penglu
 * @version 1.0.0
 * @date 2019-08-17
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public class DateUtils {

    public static long differSeconds(final Date start, final Date end) {
        return new Duration(new DateTime(start), new DateTime(end)).getStandardSeconds();
    }

    public static long differDays(final Date start, final Date end) {
        return new Duration(new DateTime(start), new DateTime(end)).getStandardDays();
    }

    public static Date parseDate(String dateStr) {
        return DateTimeFormat.forPattern(PayConstants.FULL_STANDARD_PATTERN).parseDateTime(dateStr).toDate();
    }

    public static String formatDate(Date date) {
        return new DateTime(date).toString(PayConstants.FULL_STANDARD_PATTERN);
    }

}
