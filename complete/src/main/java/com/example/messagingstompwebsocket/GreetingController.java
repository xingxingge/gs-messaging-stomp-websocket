package com.example.messagingstompwebsocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

@RestController
@RequestMapping("/1232")
public class GreetingController {

  @Autowired
  private SimpMessagingTemplate messagingTemplate;

  @MessageMapping("/hello")
  @SendTo("/topic/greetings")
  public Greeting greeting(@Payload HelloMessage message) throws Exception {
    Thread.sleep(1000); // simulated delay
    return new Greeting(
        "Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!");
  }

  @MessageMapping("/hello2")
  @SendToUser(value = "/topic/greetings", broadcast = false)
  public Greeting greeting(@Payload HelloMessage message,
      SimpMessageHeaderAccessor sha) throws Exception {
    Thread.sleep(1000); // simulated delay
    Greeting greeting = new Greeting(
        "Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!");
    //		String username=sha.getUser().getName();
    //				messagingTemplate.convertAndSendToUser(username,"/topic/greetings",greeting);
    System.out.println(sha.getSessionId());
    messagingTemplate
        .convertAndSendToUser(sha.getSessionId(), "/topic/greetings", greeting,
            sha.getMessageHeaders());
    return greeting;
  }

  @MessageMapping("/hello3")
  public Greeting greeting3(@Payload HelloMessage message,
      SimpMessageHeaderAccessor sha) throws Exception {
    Thread.sleep(1000); // simulated delay
    Greeting greeting = new Greeting(
        "Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!");
    String username = sha.getUser().getName();
    messagingTemplate.convertAndSend("/topic/greetings", greeting);
    return greeting;

  }

}
