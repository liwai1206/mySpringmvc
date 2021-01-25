package com.yc.spring.mvc.core;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 *  请求映射对象
 *  每一个映射地址都是一个对象
 *
 * @author 外哥
 * @Description:
 * @email : liwai2012220663@163.com
 * @date 2021/1/23 15:34
 */
public class HandlerMapperInfo {
    // 处理这个请求的方法
    private Method method ;
    // 这个方法所属的对象method.invoke(obj,args)
    private Object obj ;
    // 这个方法需要的形参列表
    private Object[] args ;

    @Override
    public String toString() {
        return "HandlerMapperInfo{" +
                "method=" + method +
                ", obj=" + obj +
                ", args=" + Arrays.toString(args) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HandlerMapperInfo that = (HandlerMapperInfo) o;
        return method.equals(that.method) &&
                obj.equals(that.obj) &&
                Arrays.equals(args, that.args);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(method, obj);
        result = 31 * result + Arrays.hashCode(args);
        return result;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
