package vn.socialmedia.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.socialmedia.dto.request.CreateConversationRequest;
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
            @RequestParam Long userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        return new ResponseData<>(HttpStatus.OK.value(), "Get conversations successfully", conversationService.getConversations(userId, cursor, limit));
    }

    @GetMapping("/{conversationId}")
    public ResponseData<?> getConversationById(@PathVariable Long conversationId) {
        return new ResponseData<>(HttpStatus.OK.value(), "Get conversation successfully", conversationService.getConversationById(conversationId));
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseData<?> getMessages(@PathVariable Long conversationId) {
        return new ResponseData<>(HttpStatus.OK.value(), "Get messages successfully", messageService.getMessages(conversationId));
    }

    @GetMapping("/total-unread")
    public ResponseData<?> countUnreadConversations(@RequestParam Long userId) {
        return new ResponseData<>(HttpStatus.OK.value(), "Count unread conversation successfully", messageService.countUnreadConversations(userId));
    }

    @PostMapping
    public ResponseData<?> createConversation(@Valid @RequestBody CreateConversationRequest request) {
        return new ResponseData<>(HttpStatus.CREATED.value(), "Create conversation successfully", conversationService.createConversation(request));
    }
}
