package com.xingyun.bbc.mall.base.utils;

public class GenerateRandomNumUtil{

	/**
	 *
	 * @Title: generateAuthNum
	 * @Description: 生成随机数字验证码(纯数字)
	 * @param authLen
	 *            验证码位数
	 * @return 验证码
	 * @author Thstone
	 */

	public static String generateAuthNum(int authLen) {
		int num = (int) (Math.random() * Math.pow(10, authLen));
		String numStr = String.valueOf(num);
		StringBuilder buil = new StringBuilder(authLen);
		// 生成authLen位数验证码,不足的左边补0
		for (int i = numStr.length(); i < authLen; i++) {
			buil.append('0');
		}
		buil.append(numStr);
		return String.valueOf(buil);
	}
}