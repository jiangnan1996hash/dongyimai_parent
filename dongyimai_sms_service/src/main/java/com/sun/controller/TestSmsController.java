package com.sun.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.jms.*;

/**
 *  此项目单独项目：localhost:9006/sms/sendMsg.do?mobile=手机号&param=验证码
 *
 */

@RestController
@RequestMapping("/sms")
public class TestSmsController {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Destination smsDestination;


    /**
     * 发送消息 到消费端
     */
    @RequestMapping("/sendMsg")
    public String sendMsg(final String mobile,final String param){
        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage mapMessage = session.createMapMessage();
                mapMessage.setString("mobile",mobile);
                mapMessage.setString("param",param);
                System.out.println("电话号码和手机号");
                System.out.println(mobile+" "+param);
                return mapMessage;
            }
        });

        return "send ok";
    }







}
