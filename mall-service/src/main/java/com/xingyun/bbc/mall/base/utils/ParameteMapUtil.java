package com.xingyun.bbc.mall.base.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;

import com.google.common.base.Strings;


/**
 * request 工具类
* @ClassName: ParameteMapUtil 
* @Description: TODO(这里用一句话描述这个类的作用) 
* @author sup
* @date 2016年9月7日 下午12:09:02 
*
 */
public class ParameteMapUtil {
	
	
	/**
	 * 
	* @Title: 从HttpServletRequest中取出所有参数 
	* @Description: 注意: 会对每个参数trim()
	* @param request
	* @return Map<String, String>
	* @author Tito
	 */
	public static Map<String, String> getParameters(HttpServletRequest request) {
		return getParameters(request, true);
	}
	
	/**
	 * 
	* @Title: 从HttpServletRequest中取出所有参数 
	* @Description:
	* @param request
	* @param isTrim 是否需要对每个参数trim()
	* @return Map<String, String>
	* @author Tito
	 */
	public static Map<String, String> getParameters(HttpServletRequest request, boolean isTrim) {
		Map<String, String[]> parameterMap = request.getParameterMap();
		Map<String, String> params = new HashMap<String, String>();
		for (Entry<String, String[]> entry : parameterMap.entrySet()) {
			String[] valueArr = entry.getValue();
			if (valueArr == null || valueArr.length == 0)
				continue;
			String key = entry.getKey();
			String value = "";
			for (int idx = 0, len = valueArr.length; idx < len; idx++) {
				String v = valueArr[idx];
				v = v == null ? "" : (isTrim ? v.trim() : v);
				if (v.length() > 0)
					value += "," + v;
			}
			if (value.length() > 0) 
				params.put(key, value.substring(1));
		}
		return params;
	}

	/**
	 * 
	* @Title: getParameterMap 
	* @Description: TODO(对request转化成map) 
	* @param @param request
	* @param @return  参数说明 
	* @return Map    返回类型 
	* @author Administrator
	* @throws
	 */
	@SuppressWarnings("unchecked")
	public static Map getParameterMap(HttpServletRequest request) {
	    // 参数Map
	    Map properties = request.getParameterMap();
	    System.out.println("param size:"+properties.size());
	    // 返回值Map
	    Map returnMap = new HashMap();
	    Iterator entries = properties.entrySet().iterator();
	    Map.Entry entry;
	    
	    while (entries.hasNext()) {
	    	String name = "";
	    	String value = "";
	        entry = (Map.Entry) entries.next();
	        name = (String) entry.getKey();
	        Object valueObj = entry.getValue();
	        if(StringUtils.isEmpty(valueObj)){
	            value = "";
	        }else if(valueObj instanceof String[]){
	            String[] values = (String[])valueObj;
	            for(int i=0;i<values.length;i++){
	            	if(!Strings.isNullOrEmpty(values[i].trim())){
	            		value += values[i] + ",";
	            	}
	            }
	            if(!Strings.isNullOrEmpty(value)){
	            	value = value.substring(0, value.length()-1);
	            }
	        }else{
	            value = valueObj.toString();
	        }
	        if(!Strings.isNullOrEmpty(value)){
	        	returnMap.put(name, value);
	        }
	    }
	    return returnMap;
	}
	
	
	/**
	 * 
	* @Title: getDesignatedParams 
	* @Description: 从Request获取指定名称的参数
	* @param request
	* @param paramNameArr	参数名称数组
	* @return
	* @author Tito
	 */
	public static Map<String, Object> getDesignatedParams(HttpServletRequest request, String[] paramNameArr) {
		Map<String, Object> params = new HashMap<>();
		if (paramNameArr == null || paramNameArr.length == 0) {
			return params;
		}
		for (String paramName : paramNameArr) {
			String paramValue = request.getParameter(paramName);
			if (Strings.isNullOrEmpty(paramValue)) {
				if ("pageIndex".equals(paramName)) {
					params.put(paramName, 1);
				} else if ("pageSize".equals(paramName)) {
					params.put(paramName, 20);
				}
				continue;
			}
			if ("pageIndex".equals(paramName) || "pageSize".equals(paramName)) {
				params.put(paramName, Integer.parseInt(paramValue));
			} else {
				params.put(paramName, paramValue.trim());
			}
		}

		return params;
	}
	
	/**
	 * 
	* @Title: checkParamsNotEmpty 
	* @Description: 校验参数都不为空
	* @param params
	* @return
	* @author Tito
	 */
	public static boolean checkParamsNotEmpty(String... params) {
		if (params == null || params.length == 0) {
			return false;
		}
		for (String param : params) {
			if (Strings.isNullOrEmpty(param)) {
				return false;
			}
		}
		return true;
	}
}
