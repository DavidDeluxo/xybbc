/**   
* @Title: DecodeUtil.java 
* @Package com.xingyun.xyb2b.common.util 
* @Description: TODO
* @author leo
* @date 2017年1月5日 下午3:43:13 
* @company 版权所有 深圳市天行云供应链有限公司
* @version V1.0   
*/
package com.xingyun.bbc.mall.base.utils;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
* @ClassName: DecodeUtil 
* @Description: 解密
* @author leo
* @date 2017年1月5日 下午3:43:13 
*  
*/
public class DecodeUtil {
	private static Logger logger =	 LoggerFactory.getLogger(DecodeUtil.class);
	/** 
	* @Title: decodeApp 
	* @Description: 移动端解码 
	* @param pwd
	* @return
	* @author leo
	*/
	public static String decodeApp(String pwd){
		return decode(pwd,5,3);
	}
	
	/** 
	* @Title: decodeWeb 
	* @Description: pc端解码 
	* @param pwd
	* @return
	* @author leo
	*/
	public static String decodeWeb(String pwd){
		return decode(pwd,5,3);
	}
	
	/** 
	* @Title: decode 
	* @Description: 解密 
	* @param pwd 密码
	* @param prefix 截取前多少个字符
	* @param suffix 截取后多少个字符
	* @return
	* @author leo
	*/
	private static String decode(String pwd,int prefix,int suffix){
		String password="";
		try {
			password=new String(Base64.getDecoder().decode(pwd), "utf-8");
			password=password.substring(prefix);
			password=password.substring(0,password.length()-suffix);
			password=new String(Base64.getDecoder().decode(password),"utf-8");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("解码报错，密码为："+pwd);
		}
		return password;
	}
}
