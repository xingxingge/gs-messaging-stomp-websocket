package com.example.messagingstompwebsocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic");
    config.setApplicationDestinationPrefixes("/app");
    config.setUserDestinationPrefix("/user");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/gs-guide-websocket")
        .setHandshakeHandler(new CustomHandshakeHandler()).withSockJS();
  }

  @EventListener
  public void handleDisconnectEvent(SessionDisconnectEvent event) {
    //获取session挂关闭事件,关闭listener
    System.out.println("close session: " + event.getUser().getName());

  }

  @EventListener
  public void handleconnectedEvent(SessionConnectedEvent event) {
    GenericMessage message = (GenericMessage) event.getMessage();
    MessageHeaders headers = message.getHeaders();
    Object simpSessionId = headers.get("simpSessionId");
    System.out.println("open sessionId: " + event.getUser().getName());
    //    StompHeaderAccessor sha1 = StompHeaderAccessor.wrap(message);
    //    String userId = sha1.getNativeHeader("userId").get(0);
    //    System.out.println(userId);
  }

}
