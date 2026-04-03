package vn.socialmedia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.socialmedia.common.security.SecurityUtil;
import vn.socialmedia.dto.request.MessageCreationRequest;
import vn.socialmedia.dto.response.MessageResponse;
import vn.socialmedia.dto.response.UserSummary;
import vn.socialmedia.enums.ErrorCode;
import vn.socialmedia.exception.BusinessException;
import vn.socialmedia.model.Conversation;
import vn.socialmedia.model.ConversationParticipant;
import vn.socialmedia.model.Message;
import vn.socialmedia.model.User;
import vn.socialmedia.repository.ConversationParticipantRepository;
import vn.socialmedia.repository.ConversationRepo;
import vn.socialmedia.repository.MessageRepo;
import vn.socialmedia.service.ChatViewStateService;
import vn.socialmedia.service.MessageService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "MESSAGE-SERVICE")
public class MessageServiceImpl implements MessageService {

    private final MessageRepo messageRepo;
    private final SimpMessageSendingOperations messageTemplate;
    private final ConversationRepo conversationRepo;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private final ChatViewStateService chatViewStateService;

    @Transactional
    @Override
    public void createMessage(MessageCreationRequest request) {
        if (isEmptyMessageRequest(request)) {
            return;
        }

        User currentUser = SecurityUtil.getUser();
        Conversation conversation = conversationRepo.findById(request.getConversationId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CONVERSATION_NOT_FOUND, request.getConversationId()));

        conversationParticipantRepository.findByConversation_IdAndUser_Id(conversation.getId(), currentUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_PERMISSION));

        Message message = Message.builder()
                .message(request.getMessage())
                .mediaUrl(request.getMediaUrl())
                .conversation(conversation)
                .user(currentUser)
                .build();

        message = messageRepo.save(message);

        conversation.setLastMessageAt(message.getCreatedAt() != null ? message.getCreatedAt() : LocalDateTime.now());
        conversationRepo.save(conversation);

        List<ConversationParticipant> recipients = conversationParticipantRepository
                .findParticipantsExcludingUser(conversation.getId(), currentUser.getId());

        List<String> recipientUsernames = new ArrayList<>(recipients.size());
        boolean allRecipientsViewing = !recipients.isEmpty();

        for (ConversationParticipant participant : recipients) {
            User participantUser = participant.getUser();
            recipientUsernames.add(participantUser.getUsername());

            boolean isViewing = chatViewStateService.isViewingConversation(participantUser.getUsername(), conversation.getId());
            if (isViewing) {
                markSeenUpTo(participant, message);
            } else {
                incrementUnread(participant);
                allRecipientsViewing = false;
                incrementUnreadConversations(participantUser);
            }
        }

        MessageResponse response = MessageResponse.builder()
                .id(message.getId())
                .message(message.getMessage())
                .mediaUrl(message.getMediaUrl())
                .isRead(allRecipientsViewing)
                .createdAt(message.getCreatedAt())
                .sender(userSummaryBuilder(currentUser))
                .conversationId(conversation.getId())
                .build();

        for (String username : recipientUsernames) {
            broadcastMessage(response, username);
        }
    }

    @Override
    public List<MessageResponse> getMessages(Long conversationId) {
        Conversation conversation = conversationRepo.findById(conversationId).orElseThrow(() -> new BusinessException(ErrorCode.CONVERSATION_NOT_FOUND, conversationId));

        return conversation.getMessages()
                .stream()
                .map(this::messageResponseBuilder)
                .toList();
    }

    @Override
    public int countUnreadConversations(long userId) {
        return conversationParticipantRepository.countUnreadConversations(userId);
    }

    private void broadcastMessage(MessageResponse messageResponse, String recipientUsername) {
        messageTemplate.convertAndSendToUser(recipientUsername, "/queue/messages", messageResponse);
    }

    private MessageResponse messageResponseBuilder(Message message) {
        User currentUser = SecurityUtil.getUser();
        // For messages sent by the current user, read means all other participants have read up to this message.
        boolean isRead = message.getConversation().getParticipants().stream()
                .filter(cp -> !cp.getUser().getId().equals(message.getUser().getId())) // filter: user is not sender
                .allMatch(cp -> cp.getLastReadMessage() != null && cp.getLastReadMessage().getId() >= message.getId());

        if (!message.getUser().getId().equals(currentUser.getId())) {
            // For received messages, read should reflect the current viewer's own read position instead.
            ConversationParticipant selfParticipant = conversationParticipantRepository
                    .findByConversation_IdAndUser_Id(message.getConversation().getId(), currentUser.getId())
                    .orElse(null);

            if (selfParticipant == null) {
                isRead = false;
            } else {
                Message lastReadMessage = selfParticipant.getLastReadMessage();

                if (lastReadMessage == null) {
                    isRead = false;
                } else {
                    isRead = lastReadMessage.getId() >= message.getId();
                }
            }
        }

        return MessageResponse.builder()
                .id(message.getId())
                .message(message.getMessage())
                .mediaUrl(message.getMediaUrl())
                .isRead(isRead)
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

    private boolean isEmptyMessageRequest(MessageCreationRequest request) {
        boolean noText = request.getMessage() == null || request.getMessage().isBlank();
        boolean noMedia = request.getMediaUrl() == null || request.getMediaUrl().isBlank();
        return noText && noMedia;
    }

    private void markSeenUpTo(ConversationParticipant participant, Message message) {
        Message lastRead = participant.getLastReadMessage();
        if (lastRead != null && lastRead.getId() >= message.getId()) {
            return;
        }

        participant.setLastReadMessage(message);
        participant.setLastReadAt(LocalDateTime.now());
        participant.setUnreadCount(0);
        conversationParticipantRepository.save(participant);
    }

    private void incrementUnread(ConversationParticipant participant) {
        int currentUnread = participant.getUnreadCount() == null ? 0 : participant.getUnreadCount();
        participant.setUnreadCount(currentUnread + 1);
        conversationParticipantRepository.save(participant);
    }

    private void incrementUnreadConversations(User user) {
        int unreadConversations = countUnreadConversations(user.getId());
        messageTemplate.convertAndSendToUser(user.getUsername(), "/queue/unreadConversations", unreadConversations);
    }
}
