package com.sun.search.listener.impl;

import com.alibaba.fastjson.JSON;
import com.sun.pojo.TbItem;
import com.sun.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;
import java.util.Map;

public class ItemSearchListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message){
        try {
            System.out.println("监听接收到消息...");
            TextMessage textMessage = (TextMessage) message;
            String text = textMessage.getText();
            List<TbItem> itemList = JSON.parseArray(text,TbItem.class);
            for (TbItem item:itemList) {
                System.out.println(item.getId()+" "+item.getTitle());
                //将spec字段中的json字符串转换成Map
                Map specMap = JSON.parseObject(item.getSpec(), Map.class);
                item.setSpecMap(specMap);//给带个注解的字段赋值
            }
            itemSearchService.importList(itemList);//导入索引库
            System.out.println("成功导入到索引库");

        }catch (Exception e){
            e.printStackTrace();
        }


    }





}
