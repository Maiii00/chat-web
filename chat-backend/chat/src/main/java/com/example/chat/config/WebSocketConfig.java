package com.example.chat.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;



@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private WebSocketUserInterceptor webSocketUserInterceptor;
    
    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
        // 啟用訊息代理
        registry.enableSimpleBroker("/topic", "/queue");
        // 設定前綴，客戶端要發送訊息時，目的地是 "/app"
        registry.setApplicationDestinationPrefixes("/app");
        // 啟用 "/user/queue/"，讓特定用戶可以接收點對點訊息
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // 註冊 WebSocket 端點，讓客戶端能夠連線
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketUserInterceptor);
    }
}
