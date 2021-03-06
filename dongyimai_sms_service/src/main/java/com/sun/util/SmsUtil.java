package com.sun.util;

import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class SmsUtil {

    @Value("${AppCode}")
    private String appcode;

    @Value("${tpl}")
    private String tpl;

    //信息链接地址
    private String host = "http://dingxin.market.alicloudapi.com";

    public HttpResponse sendSms(String mobile,String param) throws Exception {
        String path = "/dx/sendSms";
        String method = "POST";

        Map<String, String> headers = new HashMap<String,String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String,String>();
        querys.put("mobile", mobile);
        querys.put("param", "code:"+param);
        querys.put("tpl_id", tpl);
        Map<String, String> bodys = new HashMap<String,String>();

        /**
         * 重要提示如下:
         * HttpUtils请从
         * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
         * 下载相应的依赖请参照
         * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
         */
        HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
        System.out.println(response.toString());
        return response;
        //获取response的body
        //System.out.println(EntityUtils.toString(response.getEntity()));

    }





}
