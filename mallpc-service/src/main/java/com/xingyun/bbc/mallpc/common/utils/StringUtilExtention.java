package com.xingyun.bbc.mallpc.common.utils;

import com.xingyun.bbc.core.utils.StringUtil;
import net.sourceforge.pinyin4j.PinyinHelper;

/**
 * @author nick
 * @ClassName: StringUtilExtention
 * @Description: StringUtil 拓展
 * @date 2019年08月20日 11:28:08
 */
public class StringUtilExtention extends StringUtil {

    /**
     * @param
     * @return
     * @Description :判断字符串是否是纯英文字符串
     * @author :nick
     * @Date :2019-08-20 11:29
     */

    public static boolean isEnglish(String str) {
        return str.matches("^[a-zA-Z]*");
    }

    /**
     * @param
     * @return
     * @Description :判断字符串是否是纯数字字符串
     * @author :nick
     * @Date :2019-08-20 11:29
     */

    public static boolean isNumeric(String str) {
        return str.matches("^[0-9]*");
    }

    /**
     *  @Description :获取汉字首个拼音
     *  @author :nick
     *  @Date :2019-08-20 12:31
     *  @param
     *  @return
     */
    public static String getHanziInitials(String hanzi) {
        String result = null;
        if(null != hanzi && !"".equals(hanzi)) {
            char[] charArray = hanzi.toCharArray();
            StringBuffer sb = new StringBuffer();
            for (char ch : charArray) {
                // 逐个汉字进行转换， 每个汉字返回值为一个String数组（因为有多音字）
                String[] stringArray = PinyinHelper.toHanyuPinyinStringArray(ch);
                if(null != stringArray) {
                    sb.append(stringArray[0].charAt(0));
                }
            }
            if(sb.length() > 0) {
                result = sb.toString().toUpperCase();
            }
        }
        return result;
    }

}
