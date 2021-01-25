package com.yc.spring.mvc.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 外哥
 * @Description:
 * @email : liwai2012220663@163.com
 * @date 2021/1/23 14:53
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResponseBody {
    public String value() default "" ;
}
