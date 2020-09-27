package com.sun.sellergoods.controller;


import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/login")
public class LoginController {

    //从springsecurity中获取当前登录的用户信息
    @RequestMapping("/name")
    public Map name(){
        Map<String,String> map = new HashMap<String,String>();
        //从springsecurity中获取当前登录的用户信息
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("loginName",name);

        return map;
    }


}
