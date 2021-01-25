package com.yc.spring.mvc.core;

/**
*
* @author : 外哥
* 邮箱 ： liwai2012220663@163.com
* 创建时间:2021年1月10日 下午4:56:40
*/
public class StringUtil {
	
	/**
	 * 判断字符串是否为空
	 * @param args	量词参数
	 * @return
	 */
	public static boolean checkNull( String ... args) {
		
		for (int i = 0; i < args.length; i++) {
			if ( args[i]== null || "".equals(args[i]) ) {
				return true ;
			}
		}	
		
		return false ;
	}
}
