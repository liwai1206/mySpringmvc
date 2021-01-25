package com.yc.spring.mvc.controller;

import com.yc.spring.mvc.core.annotation.*;
import com.yc.spring.mvc.domain.UserInfo;
import com.yc.spring.mvc.service.IUserService;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 外哥
 * @Description:
 * @email : liwai2012220663@163.com
 * @date 2021/1/23 15:02
 */
@Controller
@RequestMapping("/user")
public class UserInfoController {

    @Autowired
    private IUserService userService ;

    public IUserService getUserService() {
        return userService;
    }

    @RequestMapping("/add")
    @ResponseBody
    public UserInfo add( UserInfo  uf  ){
        System.out.println( "hello,this is add method ");
        return uf ;
    }

    @RequestMapping("/login")
    @ResponseBody
    public UserInfo login(UserInfo uf , HttpSession session ){
        System.out.println( uf );
        System.out.println( session );
        return uf ;
    }

    @RequestMapping("/finds")
    @ResponseBody
    public List<UserInfo> finds(Map<String,Object> map ){
        System.out.println( map );
        List<UserInfo> list = new ArrayList<>() ;
        return list ;
    }

    @RequestMapping("/check")
    @ResponseBody
    public UserInfo login(@RequestParam("account") String name , String pwd ){
        System.out.println( name + "\t" + pwd );
        return null ;
    }


}
