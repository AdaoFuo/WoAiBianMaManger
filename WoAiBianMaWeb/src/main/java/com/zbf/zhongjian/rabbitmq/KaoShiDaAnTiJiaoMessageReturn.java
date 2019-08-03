package com.zbf.zhongjian.rabbitmq;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 作者：LCG
 * 创建时间：2019/2/22 15:40
 * 描述：
 */
@Component
public class KaoShiDaAnTiJiaoMessageReturn implements RabbitTemplate.ReturnCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init(){
        rabbitTemplate.setReturnCallback ( this );
    }

    //处理失败返回
    @Override
    public void returnedMessage(Message message, int i, String s, String s1, String s2) {

        System.out.println ("----returnedMessage--->"+message.toString ()+"----"+i+"----"+s+"----"+s1+"-----"+s2);

    }

}
