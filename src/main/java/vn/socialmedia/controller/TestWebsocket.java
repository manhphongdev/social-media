package vn.socialmedia.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class TestWebsocket {

    @MessageMapping("/chat.send")
    @SendTo("/topic/notifications")
    public String send(String msg) {
        log.info("Received WebSocket message: {}", msg);
        return "Echo: " + msg;
    }

}
