package com.zbf.web;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 作者：LCG
 * 创建时间：2019/2/22 11:18
 * 描述：
 */
@RestController
public class RabbitMQController {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RequestMapping("testrabbit")
    public void test(){
        CorrelationData correlationData=new CorrelationData ( UUID.randomUUID ().toString () );
        rabbitTemplate.convertAndSend ( "tijiaoshijuandaan-exchange","tijiaoshijuandaan-key","123456",correlationData);

    }

}
