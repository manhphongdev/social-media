package vn.socialmedia.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import vn.socialmedia.service.ChatViewStateService;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatViewController {

    private final ChatViewStateService chatViewStateService;

    @MessageMapping("/chat/view/start")
    public void startViewing(Long conversationId,
                             Principal principal,
                             SimpMessageHeaderAccessor headerAccessor) {
        String username = principal.getName();
        String sessionId = headerAccessor.getSessionId();
        chatViewStateService.startViewingConversation(username, conversationId, sessionId);
    }

    @MessageMapping("/chat/view/stop")
    public void stopViewing(Long conversationId,
                            Principal principal,
                            SimpMessageHeaderAccessor headerAccessor) {
        String username = principal.getName();
        String sessionId = headerAccessor.getSessionId();
        chatViewStateService.stopViewingConversation(username, conversationId, sessionId);
    }

    @MessageMapping("/chat/view/ping")
    public void pingViewing(Long conversationId,
                            Principal principal,
                            SimpMessageHeaderAccessor headerAccessor) {
        String username = principal.getName();
        String sessionId = headerAccessor.getSessionId();
        chatViewStateService.startViewingConversation(username, conversationId, sessionId);
    }
}
