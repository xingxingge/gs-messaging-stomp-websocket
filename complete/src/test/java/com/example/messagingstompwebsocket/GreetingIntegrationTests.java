package com.example.messagingstompwebsocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GreetingIntegrationTests {

  //	@LocalServerPort
  private int port = 8080;

  private SockJsClient sockJsClient;

  private WebSocketStompClient stompClient;

  private final WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

  @BeforeEach
  public void setup() {
    List<Transport> transports = new ArrayList<>();
    transports.add(new WebSocketTransport(new StandardWebSocketClient()));
    this.sockJsClient = new SockJsClient(transports);

    this.stompClient = new WebSocketStompClient(sockJsClient);
    this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());
  }

  @Test
  public void getGreeting() throws Exception {

    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<Throwable> failure = new AtomicReference<>();

    StompSessionHandler handler = new TestSessionHandler(failure) {

      @Override
      public void afterConnected(final StompSession session,
          StompHeaders connectedHeaders) {
        System.out.println("sessionId: " + session.getSessionId());
        session.subscribe("/user/topic/greetings", new StompFrameHandler() {
          @Override
          public Type getPayloadType(StompHeaders headers) {
            return Greeting.class;
          }

          @Override
          public void handleFrame(StompHeaders headers, Object payload) {
            Greeting greeting = (Greeting) payload;
            try {
              System.out.println(greeting.getContent());
              assertEquals("Hello, Spring!", greeting.getContent());
            } catch (Throwable t) {
              failure.set(t);
            } finally {
              session.disconnect();
              latch.countDown();
            }
          }
        });
        try {
          session.send("/app/hello2", new HelloMessage("Spring"));
        } catch (Throwable t) {
          failure.set(t);
          latch.countDown();
        }
      }
    };

    this.stompClient
        .connect("ws://localhost:{port}/gs-guide-websocket", this.headers,
            handler, this.port);

    if (latch.await(3, TimeUnit.SECONDS)) {
      if (failure.get() != null) {
        throw new AssertionError("", failure.get());
      }
    } else {
      fail("Greeting not received");
    }

  }

  private class TestSessionHandler extends StompSessionHandlerAdapter {

    private final AtomicReference<Throwable> failure;

    public TestSessionHandler(AtomicReference<Throwable> failure) {
      this.failure = failure;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
      this.failure.set(new Exception(headers.toString()));
    }

    @Override
    public void handleException(StompSession s, StompCommand c, StompHeaders h,
        byte[] p, Throwable ex) {
      this.failure.set(ex);
    }

    @Override
    public void handleTransportError(StompSession session, Throwable ex) {
      this.failure.set(ex);
    }
  }
}
