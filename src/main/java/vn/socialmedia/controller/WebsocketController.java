package vn.socialmedia.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import vn.socialmedia.service.OnlineStatusService;

@Controller
@RequiredArgsConstructor
public class WebsocketController {

    private final OnlineStatusService onlineStatusService;

    @MessageMapping("/user-online")
    public void userOnline(String username) {
        onlineStatusService.isOnline(username);
    }
}
