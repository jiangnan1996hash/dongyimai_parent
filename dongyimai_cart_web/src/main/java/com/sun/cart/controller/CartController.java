package com.sun.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.sun.cart.service.CartService;
import com.sun.entity.Result;
import com.sun.group.Cart;
import com.sun.util.CookieUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference(timeout = 10000)
    private CartService cartService;

//    @Autowired
//    private HttpServletRequest request;

//    @Autowired
//    private HttpServletResponse response;

    /**
     * 查询购物车
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(HttpServletRequest request,HttpServletResponse response){
        //获取登录人名称
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("username:"+username);

        String cartListString = CookieUtil.getCookieValue(request, "cartList","UTF-8");
        if(cartListString==null || cartListString.equals(" ")){
            cartListString="[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);

        //判断是否为匿名用户
        if(username.equals("anonymousUser")){
            return cartList_cookie;
        }else{
            //如果已经登录，从缓存读数据
            List<Cart> cartList_redis =cartService.findCartListFromRedis(username);//从redis中提取
            if(cartList_cookie.size()>0){//如果本地存在购物车
                //合并购物车
                cartList_redis=cartService.mergeCartList(cartList_redis, cartList_cookie);
                //清除本地cookie的数据
                CookieUtil.deleteCookie(request, response, "cartList");
                //将合并后的数据存入redis
                cartService.saveCartListToRedis(username, cartList_redis);
            }
            return cartList_redis;
        }
    }


    /**
     * 添加商品到购物车
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(HttpServletRequest request,HttpServletResponse response,
                                     Long itemId,Integer num){
        //允许 地址为 localhost 端口为9105的地址进行请求
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
        //允许携带参数的请求
        response.setHeader("Access-Control-Allow-Credentials", "true");
        //获取登录人名称
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("username:"+username);
        try {
            //获取购物车列表
            List<Cart> oldcartList = findCartList(request,response);

            //新的商品sku添加到购物车中
            List<Cart> newcartsList = cartService.addGoodsToCartList(oldcartList, itemId, num);

            if(username.equals("anonymousUser")){
                //如果没有登录 将新的购物车列表存入cookie中
                System.out.println("未登录将数据保存到cookie中");
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(newcartsList),
                        3600 * 24, "UTF-8");

            }else{
                //如果登录了，存到reids中
                System.out.println("登录了将数据保存到redis中");
                cartService.saveCartListToRedis(username,newcartsList);

            }
            return new Result(true, "添加成功");
        }catch (RuntimeException e){
            e.printStackTrace();
            return new Result(false, e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false, "添加失败");
        }

    }

}
