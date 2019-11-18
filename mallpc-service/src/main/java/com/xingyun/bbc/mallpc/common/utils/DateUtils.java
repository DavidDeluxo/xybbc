package com.xingyun.bbc.mallpc.common.utils;

import com.xingyun.bbc.mallpc.common.constants.MallPcConstants;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;

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

    public static long differMillis(final Date start, final Date end) {
        return new Duration(new DateTime(start), new DateTime(end)).getMillis();
    }

    public static long differSeconds(final Date start, final Date end) {
        return new Duration(new DateTime(start), new DateTime(end)).getStandardSeconds();
    }

    public static long differDays(final Date start, final Date end) {
        return new Duration(new DateTime(start), new DateTime(end)).getStandardDays();
    }

    public static Date parseDate(String dateStr) {
        return DateTimeFormat.forPattern(MallPcConstants.FULL_STANDARD_PATTERN).parseDateTime(dateStr).toDate();
    }
    
    public static Date parseDateToHour(String dateStr) {
      return DateTimeFormat.forPattern(MallPcConstants.FULL_STANDARD_PATTERN_HOUR).parseDateTime(dateStr).toDate();
  }

    public static Date parseDate(long timeMillis) {
        return new DateTime(timeMillis).toDate();
    }

    public static String formatDate(Date date) {
        return formatDate(date, MallPcConstants.FULL_STANDARD_PATTERN);
    }

    public static String formatDate(Date date, String dateFormat) {
        return new DateTime(date).toString(dateFormat);
    }

    public static long getCurrentMillis() {
        return new DateTime().getMillis();
    }

}
