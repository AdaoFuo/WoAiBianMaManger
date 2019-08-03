package com.zbf.zhongjian.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 作者：LCG
 * 创建时间：2019/2/22 9:40
 * 描述：该类用来配置RabbitMQ
 */
@Configuration
public class RabbitMQConfig {



    //=====================================================下面的代码是用来配置消息信息start======================
    //创建队列
    @Bean
    public Queue getQueue(){

        return new Queue ( "tijiaoshijuandaan-queue",true);
    }

    //创建交换机
    @Bean
    public DirectExchange getDirectExchange(){

        DirectExchange directExchange=new DirectExchange ( "tijiaoshijuandaan-exchange",true,false);

        return directExchange;
    }

    //创建绑定交换机和队列
    @Bean
    public Binding getBinding(Queue queue,DirectExchange directExchange){

        Binding binding = BindingBuilder.bind ( queue ).to ( directExchange ).with ( "tijiaoshijuandaan-key" );
        return binding;

    }



}
