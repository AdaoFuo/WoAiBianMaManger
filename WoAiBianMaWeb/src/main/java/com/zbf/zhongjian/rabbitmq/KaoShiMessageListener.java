package com.zbf.zhongjian.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 作者：LCG
 * 创建时间：2019/2/22 16:47
 * 描述：
 */
@Component
@RabbitListener(queues = "tijiaoshijuandaan-queue")
public class KaoShiMessageListener{

    @RabbitHandler
    public void process(Object object,Channel channel,Message message) throws IOException {

        System.out.println ("接收到的消息=1=》"+ new String(message.getBody ()));
        //消息确认
        channel.basicAck ( message.getMessageProperties ().getDeliveryTag (),false );
        System.out.println (Thread.currentThread ().getId ());
    }

}
