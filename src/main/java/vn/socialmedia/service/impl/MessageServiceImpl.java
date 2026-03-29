package vn.socialmedia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import vn.socialmedia.common.security.SecurityUtil;
import vn.socialmedia.dto.request.MessageCreationRequest;
import vn.socialmedia.dto.response.MessageResponse;
import vn.socialmedia.dto.response.UserSummary;
import vn.socialmedia.enums.ErrorCode;
import vn.socialmedia.exception.BusinessException;
import vn.socialmedia.model.Conversation;
import vn.socialmedia.model.Message;
import vn.socialmedia.model.User;
import vn.socialmedia.repository.ConversationRepo;
import vn.socialmedia.repository.MessageRepo;
import vn.socialmedia.repository.UserRepository;
import vn.socialmedia.service.ConversationService;
import vn.socialmedia.service.MessageService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "MESSAGE-SERVICE")
public class MessageServiceImpl implements MessageService {
    private final MessageRepo messageRepo;
    private final UserRepository userRepository;
    private final ConversationService conversationService;
    private final SimpMessageSendingOperations messageTemplate;
    private final ConversationRepo conversationRepo;

    @Override
    public void createMessage(MessageCreationRequest request) {
        if (request.getMessage() == null && request.getMediaUrl() == null) {
            return;
        }

        User currentUser = SecurityUtil.getUser();
        User recipientUser = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Conversation conversation = conversationService.getOrCreateConversation(currentUser, recipientUser);
        conversation.setLastMessageAt(LocalDateTime.now());

        Message message = Message.builder()
                .message(request.getMessage())
                .mediaUrl(request.getMediaUrl())
                .conversation(conversation)
                .user(currentUser)
                .build();

        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepo.save(conversation);
        messageRepo.save(message);

        broadcastMessage(MessageResponse.builder()
                        .id(message.getId())
                        .message(message.getMessage())
                        .mediaUrl(message.getMediaUrl())
                        .isRead(false)
                        .createdAt(message.getCreatedAt())
                        .sender(userSummaryBuilder(currentUser))
                        .conversationId(conversation.getId())
                        .build(),
                recipientUser.getUsername());
    }

    private void broadcastMessage(MessageResponse messageResponse, String recipientUsername) {
        messageTemplate.convertAndSendToUser(recipientUsername, "/queue/messages", messageResponse);
    }

    private UserSummary userSummaryBuilder(User user) {
        return UserSummary.builder()
                .id(user.getId())
                .displayName(user.getName())
                .avatar(user.getAvatar())
                .build();
    }

}
