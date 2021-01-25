package com.yc.spring.mvc.core;

import com.sun.org.apache.bcel.internal.generic.RETURN;
import com.yc.spring.mvc.core.annotation.ResponseBody;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * @author 外哥
 * @Description:
 * @email : liwai2012220663@163.com
 * @date 2021/1/23 15:34
 */
@SuppressWarnings("all")
public class DispatcherServlet extends HttpServlet {
    // 配置文件
    private String contextConfigLocation = null;
    private FrameworkCore frameworkCore = null;

    /**
     * 初始化方法，tomcat启动后该方法只会执行一次
     * 所以在这个方法里面我们需要读取配置文件，解析注解信息
     *
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        String temp = config.getInitParameter("contextConfigLocation");
        contextConfigLocation = StringUtil.checkNull(temp) ? "application.properties" : temp;

        // 从指定的包开始扫描，根据我们既定的规则，解析所有的注解
        frameworkCore = new FrameworkCore(contextConfigLocation);
    }

    /**
     * 前台的请求都会经过这个方法，因为当前servlet在web.xml配置会拦截所有的访问
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void service(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
        /*
          步骤 ：
            1. 获取请求地址   /mySpringmvc/user/add
            2. 获取请求的项目名 /mySpringmvc
            3. 获取请求的资源路径    /user/add
            4. 判断请求地址中是否有参数     是否含有？
            5. 根据请求路径从handlerMapper中获取处理的方法
            6. 如果获取不到，说明没有设置这个请求的处理类，则当成静态资源处理
            7. 如果有，则需要激活对应的方法处理
            8. 获取处理这个请求的具体方法
            9. 获取这个方法的形参列表，然后从请求中获取对应的形参值，即将这个请求中的参数注入到这个方法的对应形参中
            10. 反向激活这个方法，获取返回值
            11. 判断返回值以什么格式返回给前端
            12. 判断这个方法上有没有@ResponseBody注解，如果有，则以json格式返回这个结果
            13. 如果没有，则判断是否以"redirect:"开头，如果是，则以重定向方式跳转页面
            14. 否则默认以内部转发的方法跳转页面
         */

        try {
            // 首先强转
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) resp;

            // 客户端请求地址
            String uri = null;
            // 项目名
            String contextPath = null;
            // 映射地址
            String requestMapping = null;
            // 对应的映射对象
            HandlerMapperInfo handlerMapper = null;
            // 前台参数
            Map<String, String[]> params = null;
            // 需要执行的方法
            Method method = null;
            // 执行方法的对象
            Object obj = null;

            // 1. 获取请求地址   /mySpringmvc/user/add
            uri = request.getRequestURI();
            params = request.getParameterMap();
            // 2. 获取请求的项目名 /mySpringmvc
            contextPath = request.getContextPath();
            // 3. 获取请求的资源路径    /user/add
            requestMapping = uri.replace(contextPath, "").replaceAll("/+" , "/");
            // 4. 判断请求地址中是否有参数     是否含有？
            // 5. 根据请求路径从 handlerMapper 中获取处理的方法
            handlerMapper = frameworkCore.getHandlerMapper(requestMapping);

            // 6. 如果获取不到，说明没有设置这个请求的处理类，则当成静态资源处理
            if (handlerMapper == null) {
                // TODO 取不到，当成静态资源处理
                HandleResponse.handlerStaticResource( response , request.getServletContext().getRealPath("") + requestMapping.substring(1) );
                return;
            }

            method = handlerMapper.getMethod() ;
            obj = handlerMapper.getObj() ;
            // 传给要执行的方法 的参数
            Object[] args = HandleRequest.handle( request, method , response );
            // 7. 如果有，则需要激活对应的方法处理
            Object result = method.invoke(obj, args);

            // 10. 判断返回值以什么格式返回给前端
            // 判断这个方法上有没有 @ResponseBody 注解，如果有，则以json格式返回这个结果
            // 如果没有，则判断是否以"redirect:"开头，如果是，则以重定向方式跳转页面
            // 否则默认以内部转发的方法跳转页面
            if ( method.isAnnotationPresent(ResponseBody.class)) {
                // 有@ResponseBody注解,以json格式返回这个结果
                HandleResponse.sendJson( response , result );
                return ;
            }

            String resultString = result.toString();
            if ( resultString.startsWith("redirect:")) {
                // 重定向
                String url = resultString.replaceFirst("redirect:","") ;

                if ( url.startsWith("/")) {
                    url = contextPath +  url ;
                }

                response.sendRedirect( url );
            } else {
                // 转发
                request.getRequestDispatcher( resultString ).forward(request , response );
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }
}
