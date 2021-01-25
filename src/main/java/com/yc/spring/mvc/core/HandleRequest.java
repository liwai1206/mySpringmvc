package com.yc.spring.mvc.core;

import com.yc.spring.mvc.core.annotation.RequestParam;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 外哥
 * @Description:
 * @email : liwai2012220663@163.com
 * @date 2021/1/23 21:45
 */
public class HandleRequest {
    public static Object[] handle(HttpServletRequest request, Method method, HttpServletResponse response) throws IllegalAccessException, InstantiationException, ClassNotFoundException {

        // 数组的长度取决于这个方法的形参个数
        Object[] args = null;
        // 处理参数，获取这个方法的形参列表，然后从请求中获取对应的形参值，即将这个请求中的参数注入到这个方法的对应形参中
        // 获取这个方法的形参列表
        Parameter[] parameters = method.getParameters();
        args = new Object[parameters.length];
        // 参数名
        String paramName = null;
        // 参数类型
        Class<?> paramType = null;
        RequestParam requestParam = null;
        String value = null;
        Map<String, String[]> parameterValues = null ;
        Map<String,Object > paramMap = null ;

        int i = 0;

        for (Parameter parameter : parameters) {
            paramName = parameter.getName();
            paramType = parameter.getType();

            // 判断这个形参上有没有@RequestParam注解，如果有则说明等一下我们要在@RequestParam注解的value值来从请求i中获取对应的属性值，而不是通过paramName
            requestParam = parameter.getAnnotation(RequestParam.class);
            if (requestParam != null) {
                paramName = requestParam.value();
            }

            value = request.getParameter(paramName);

            // 判断形参的类型，将前台参数进行转换，再注入
            if (paramType == int.class || paramType == Integer.TYPE) {
                args[i] = Integer.parseInt(value);
            } else if (paramType == Float.class || paramType == Float.TYPE) {
                args[i] = Float.parseFloat(value);
            } else if (paramType == Double.class || paramType == Double.TYPE) {
                args[i] = Double.parseDouble(value);
            } else if ( paramType == String.class ) {
                // 是字符串类型
                 args[i] = value;
            } else if( paramType == Map.class ){
                parameterValues = request.getParameterMap();
                paramMap = new HashMap<>() ;

                for (Map.Entry<String, String[]> entry : parameterValues.entrySet()) {
                    paramMap.put( entry.getKey() , entry.getValue()[0] ) ;
                }
                args[i] = paramMap ;
            }else if ( paramType == ServletRequest.class || paramType == HttpServletRequest.class  ) {
                // 是字符串类型
                args[i] = request ;
            }else if ( paramType == ServletResponse.class || paramType == HttpServletResponse.class  ) {
                // 是字符串类型
                args[i] = response ;
            }else if ( paramType == HttpSession.class ) {
                // 是字符串类型
                args[i] = request.getSession()  ;
            }else if ( paramType == ServletContext.class ) {
                // 是字符串类型
                args[i] = request.getServletContext()  ;
            }else {
                // 默认是对象,获取该对象的class对象，给该对象的属性注值
                Class<?> cls = Class.forName(String.valueOf(paramType).split(" ")[1]);
                Object obj = cls.newInstance();
                // 获取该对象的所有属性
                Field[] fields = cls.getDeclaredFields();

                // 循环所有属性
                for (Field field : fields) {
                    // 暴力反射
                    field.setAccessible( true );
                    value = request.getParameter(field.getName()) ;
                    parameterValues = request.getParameterMap();
                    for (Map.Entry<String, String[]> entry : parameterValues.entrySet()) {
                        if ( field.getName().equals( entry.getKey() )) {
                            field.set( obj , value );
                        }
                    }
                }
                args[i] = obj ;

            }
            i++;
        }
        return args ;
    }
}
