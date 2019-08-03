package com.zbf.zhongjian.webSocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * 作者：LCG
 * 创建时间：2019/2/22 23:38
 * 描述：这是WebSocket的配置类
 */
@Configuration
public class WebSocketConfig {

    @Bean
    public ServerEndpointExporter getServerEndpointExporter(){

        return new ServerEndpointExporter ();
    }

}
