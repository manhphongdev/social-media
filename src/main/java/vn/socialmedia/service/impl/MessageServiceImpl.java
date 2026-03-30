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
import vn.socialmedia.repository.ConversationParticipantRepository;
import vn.socialmedia.repository.ConversationRepo;
import vn.socialmedia.repository.MessageRepo;
import vn.socialmedia.repository.UserRepository;
import vn.socialmedia.service.ChatViewStateService;
import vn.socialmedia.service.ConversationService;
import vn.socialmedia.service.MessageService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "MESSAGE-SERVICE")
public class MessageServiceImpl implements MessageService {

    private final MessageRepo messageRepo;
    private final UserRepository userRepository;
    private final ConversationService conversationService;
    private final SimpMessageSendingOperations messageTemplate;
    private final ConversationRepo conversationRepo;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private final ChatViewStateService chatViewStateService;

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

    @Override
    public List<MessageResponse> getMessages(Long conversationId) {
        Conversation conversation = conversationRepo.findById(conversationId).orElseThrow(() -> new BusinessException(ErrorCode.CONVERSATION_NOT_FOUND, conversationId));

        Set<Message> messages = conversation.getMessages();

        return messages
                .stream()
                .map(this::messageResponseBuilder)
                .toList();
    }

    private void broadcastMessage(MessageResponse messageResponse, String recipientUsername) {
        messageTemplate.convertAndSendToUser(recipientUsername, "/queue/messages", messageResponse);
    }

    private MessageResponse messageResponseBuilder(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .message(message.getMessage())
                .mediaUrl(message.getMediaUrl())
                .isRead(false) //TODO check isRead
                .createdAt(message.getCreatedAt())
                .sender(userSummaryBuilder(message.getUser()))
                .conversationId(message.getConversation().getId())
                .build();
    }

    private UserSummary userSummaryBuilder(User user) {
        return UserSummary.builder()
                .id(user.getId())
                .displayName(user.getName())
                .avatar(user.getAvatar())
                .build();
    }

    private int countUnreadConversations(long userId) {
        return conversationParticipantRepository.countUnreadConversations(userId);
    }

    private void incrementUnreadConversations(User user) {
        int unreadConversations = countUnreadConversations(user.getId()) + 1;
        messageTemplate.convertAndSendToUser(user.getUsername(), "/queue/unreadConversations", unreadConversations);
    }

    private User getCurrentUser() {
        return SecurityUtil.getUser();
    }

}
