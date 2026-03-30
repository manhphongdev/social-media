package vn.socialmedia.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.socialmedia.dto.response.ResponseData;
import vn.socialmedia.service.ConversationService;
import vn.socialmedia.service.MessageService;

@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
@Slf4j(topic = "CONVERSATION-CONTROLLER")
public class ConversationController {
    private final ConversationService conversationService;
    private final MessageService messageService;

    @GetMapping("/")
    public ResponseData<?> getConversation(
            @RequestParam(required = true) Long userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        return new ResponseData<>(HttpStatus.OK.value(), "Get conversations successfully", conversationService.getConversations(userId, cursor, limit));
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseData<?> getMessages(@RequestParam Long conversationId) {
        return new ResponseData<>(HttpStatus.OK.value(), "Get messages successfully", messageService.getMessages(conversationId));
    }
}
