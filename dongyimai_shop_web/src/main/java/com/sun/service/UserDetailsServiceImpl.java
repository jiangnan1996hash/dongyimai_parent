package com.sun.service;


import com.sun.pojo.TbSeller;
import com.sun.sellergoods.service.SellerService;
import javafx.scene.control.TableColumnBase;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
     * 认证类
     * @author Administrator
     *
     */
public class UserDetailsServiceImpl implements UserDetailsService {

    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
            this.sellerService = sellerService;
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("经过了UserDetailsServiceImpl");

        List<GrantedAuthority> grantAuths = new ArrayList<GrantedAuthority>();
        grantAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        TbSeller tbSeller = sellerService.findOne(username);

        if(tbSeller != null){
            if(tbSeller.getStatus().equals("1")){
                return new User(username,tbSeller.getPassword(),grantAuths);//方法中验证了用户名和密码
            }else{
                return null;
            }
        }else{
            return null;
        }

    }
}








