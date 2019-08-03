package com.zbf.zhongjian.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 作者：LCG
 * 创建时间：2019/2/22 11:41
 * 描述：这是在答案提交的时候使用的 发送的消息确认
 */
@Component
public class KaoShiMessageSenderConfirm implements RabbitTemplate.ConfirmCallback{

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init(){
        rabbitTemplate.setConfirmCallback ( this );
    }

    /**
     * 消息的发送确认回调
     * @param correlationData
     * @param b
     * @param s
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean b, String s) {
        System.out.println ("---ID--->"+correlationData);
        System.out.println ("---确认情况--->"+b);
        System.out.println ("---失败原因--->"+s);
    }


}