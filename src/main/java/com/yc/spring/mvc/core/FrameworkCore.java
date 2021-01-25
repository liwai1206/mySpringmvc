package com.yc.spring.mvc.core;

import com.yc.spring.mvc.controller.UserInfoController;
import com.yc.spring.mvc.core.annotation.Autowired;
import com.yc.spring.mvc.core.annotation.Component;
import com.yc.spring.mvc.core.annotation.Controller;
import com.yc.spring.mvc.core.annotation.RequestMapping;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.*;

/**
 * 核心代码
 * 1. 读取配置文件 -》 获取要扫描的基址路径
 * 2. 扫描包，获取类路径
 * 3. 初始化需要IoC容器管理的类，并交给IoC容器管理。例如@Component \ @ Controller
 * 4. 执行依赖注入，即完成@Autowired注解的解析
 * 5. 构建HandlerMapping处理器映射器，完成URL与对应方法之间的关联映射
 *
 * @author 外哥
 * @Description:
 * @email : liwai2012220663@163.com
 * @date 2021/1/23 15:34
 */
@SuppressWarnings("all")
public class FrameworkCore {
    // 配置文件路径
    private String contextConfigLocation;
    // 要扫描的基址路径
    private String basePackage;
    // 扫描获取到的类路径信息,即类上有@Component \ @ Controller注解
    private Set<String> classNames = new HashSet<>();
    // 用来存放需要IoC容器管理实例化好的类的对象
    private Map<String, Object> instanceObject = new HashMap<>();
    // url请求地址对应的处理对象
    private Map<String, HandlerMapperInfo> handlerMapper = new HashMap<>();

    public FrameworkCore(String contextConfigLocation) {
        this.contextConfigLocation = contextConfigLocation;
        // 初始化
        init();
    }

    /**
     * 初始化方法
     */
    private void init() {
        // TODO 1. 读取配置文件 -》 获取要扫描的基址路径
        doLoadConfig();

        // TODO 2. 扫描包，获取类路径
        doScannerObject();

//        for (String className : classNames) {
//            System.out.println(className);
//        }

//        System.out.println("======================");
        // TODO 3. 初始化需要IoC容器管理的类，并交给IoC容器管理。例如@Component \ @Controller
        doInstanceObject();

//        System.out.println(instanceObject);

//        System.out.println("======================");
//        System.out.println(handlerMapper);


        // TODO 4. 执行依赖注入，即完成@Autowired注解的解析
        doAutoWired();
//        System.out.println("======================");
//        System.out.println( (( UserInfoController)instanceObject.get("userInfoController")).getUserService() );

        // TODO 5. 构建HandlerMapping处理器映射器，完成URL与对应方法之间的关联映射
        initHandlerMapping();
    }

    /**
     * 读取配置文件 -》 获取要扫描的基址路径
     */
    private void doLoadConfig() {
        String resourceAddr = contextConfigLocation.substring(0, contextConfigLocation.indexOf("."));
        ResourceBundle bundle = ResourceBundle.getBundle(resourceAddr);
        basePackage = bundle.getString("basePackage");
    }

    /**
     * 扫描包，获取类路径
     */
    private void doScannerObject() {
        if (StringUtil.checkNull(basePackage)) {
            // 如果类路径为空,直接跑一个异常
            throw new RuntimeException("读取配置文件失败，请配置ContextConfigLocation参数一i及basePackage属性");
        }

        // 将目录中的“.”替换成“/”,从而获取该文件的资源地址
        URL url = this.getClass().getClassLoader().getResource(basePackage.replaceAll("\\.", "/"));

        // 获取该路径下的所有文件和子目录
        File dist = new File(url.getFile());

        // 递归遍历该目录下的所有文件
        getClassInfo(basePackage, dist);

    }

    /**
     * 获取指定目录下的子文件和目录
     *
     * @param basePackage
     * @param dist
     */
    private void getClassInfo(String basePackage, File dist) {

        if (dist.exists() && dist.isDirectory()) {
            // r如果当前文件存在并且是一个目录，则进行遍历
            for (File file : dist.listFiles()) {
                if (file.isDirectory()) {
                    // 如果是目录
                    getClassInfo(basePackage + "." + file.getName(), file);
                } else {
                    // 是文件
                    classNames.add(basePackage + "." + file.getName().replace(".class", ""));
                }
            }

        }

    }

    /**
     * 初始化需要IoC容器管理的类，并交给IoC容器管理。例如@Component \ @Controller
     */
    private void doInstanceObject() {
        if (classNames == null || classNames.isEmpty()) {
            // 表示没有需要IoC容器管理的类
            return;
        }

        // 获取类的字节码文件
        Class<?> cls = null;
        // 实例化这个类对象
        Object obj = null;
        // 这个bean的id属性名称
        String beanName = null;
        // 这个类实现的所有接口，实现了可以用子类给接口注入
        Class<?>[] interfaces = null;
        // 处理器映射器
        HandlerMapperInfo handlerMapperInfo = null;
        // 当前类下的所有方法
        Method[] methods = null;

        for (String className : classNames) {
            // 获取类
            try {
                cls = Class.forName(className);

                // 判断有没有@Controller注解，说明是控制器
                if (cls.isAnnotationPresent(Controller.class)) {
                    // 有@Controller注解
                    // 从Controller注解中获取bean的value值
                    beanName = cls.getAnnotation(Controller.class).value();

                    if (StringUtil.checkNull(beanName)) {
                        // value值为空
                        beanName = cls.getSimpleName().substring(0, 1).toLowerCase() + cls.getSimpleName().substring(1);
                    }

                    // 实例化该对象
                    obj = cls.newInstance();
                    // 将该类的实例化对象存入集合中
                    instanceObject.put(beanName, obj);

                    // handlerMapper的键
                    String key = "";
                    String keyClass = "";

                    // 判断类上是否有@RequestMapping注解
                    if (cls.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping annotation = cls.getAnnotation(RequestMapping.class);
                        keyClass = annotation.value();
                    }

                    // 该类下的所有方法
                    methods = cls.getDeclaredMethods();
                    if ( methods == null || methods.length <= 0 ) {
                        // 如果没有方法，则继续下一个类
                        continue;
                    }

                    // 循环该类下的所所有方法
                    for (Method method : methods) {
                        // 判断是否有RequestMapping注解，如果有的话，说明是一个映射方法
                        if (method.isAnnotationPresent(RequestMapping.class)) {
                            // 如果有
                            String value = method.getAnnotation(RequestMapping.class).value();
                            key = keyClass + value;

                            // 获取该方法的参数
                            Parameter[] parameters = method.getParameters();

                            handlerMapperInfo = new HandlerMapperInfo();
                            handlerMapperInfo.setMethod(method);
                            handlerMapperInfo.setObj(obj);

                            handlerMapper.put(key, handlerMapperInfo);
                        }
                    }


                }

                // 判断有没有@Component注解，说明有需要IoC容器实例化的对象
                if (cls.isAnnotationPresent(Component.class)) {
                    // 有@Component注解
                    // 从 Component 注解中获取bean的value值
                    beanName = cls.getAnnotation(Component.class).value();
                    if (StringUtil.checkNull(beanName)) {
                        // value值为空
                        beanName = cls.getSimpleName().substring(0, 1).toLowerCase() + cls.getSimpleName().substring(1);
                    }
                    // 实例化该对象
                    obj = cls.newInstance();
                    // 将该类的实例化对象存入集合中
                    instanceObject.put(beanName, obj);

                    // 获取该类的所有接口
                    interfaces = cls.getInterfaces();

                    // 接口为空，直接返回
                    if (interfaces == null || interfaces.length <= 0) return;

                    // 接口不为空，则循环该接口
                    for (Class<?> itf : interfaces) {
                        beanName = itf.getSimpleName();
                        // 以接口名为键，子类对象为值加入集合
                        instanceObject.put(beanName, obj);
                    }
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 执行依赖注入，即完成@Autowired注解的解析
     */
    private void doAutoWired() {
        if (instanceObject == null || instanceObject.isEmpty()) {
            // 没有交给IoC容器管理的对象
            return;
        }

        // 类的字节码文件
        Class<?> cls = null;
        // 这个类的所有字段
        Field[] fields = null;
        // bean名称
        String beanName = null;
        // autoWired注解
        Autowired autowired = null;

        // 循环所有的对象
        for (Map.Entry<String, Object> entry : instanceObject.entrySet()) {
            // 获取这个类
            cls = entry.getValue().getClass();

            // 获取这个类的所有字段
            fields = cls.getDeclaredFields();

            // 如果当前类的没有字段，则继续下一个类
            if (fields == null || fields.length <= 0) {
                continue;
            }

            // 循环所有的字段
            for (Field field : fields) {
                // 判断是否有@Autowried注解
                if (!field.isAnnotationPresent(Autowired.class)) {
                    // 没有这个注解，则继续下一个字段
                    continue;
                }

                // 获取这个注解
                autowired = field.getAnnotation(Autowired.class);
                // 获取指定的名称
                beanName = autowired.value();

                // 由于类中的字段是私有的，所以进行暴力反射一下
                field.setAccessible(true);

                if (StringUtil.checkNull(beanName)) {
                    // 名称没有给定名称，则以字段的类型名称为beanName
                    beanName = field.getType().getSimpleName();

                    // 给类中的这个字段注入值
                    try {
                        field.set(entry.getValue(), instanceObject.get(beanName));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                } else {
                    // 如果指定了名称，则判断该名称有没有对应的对象
                    if ( !instanceObject.containsKey(beanName)) {
                        // 没有这个名称的对象
                        throw new RuntimeException(cls.getName() + "." + field.getName() + "注值失败，没有对应的" + beanName);
                    }
                    // 给类中的这个字段注入值
                    try {
                        field.set(entry.getValue(), instanceObject.get(beanName));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 构建HandlerMapping处理器映射器，完成URL与对应方法之间的关联映射
     * 这一步我已经在doInstanceObject中做过了
     */
    private void initHandlerMapping() {
    }

    public String getContextConfigLocation() {
        return contextConfigLocation;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public Set<String> getClassNames() {
        return classNames;
    }

    public Map<String, Object> getInstanceObject() {
        return instanceObject;
    }

    public Map<String, HandlerMapperInfo> getHandlerMapper() {
        return handlerMapper;
    }

    public HandlerMapperInfo getHandlerMapper( String url ){
        return handlerMapper.getOrDefault( url , null ) ;
    }
}
