package vn.socialmedia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.socialmedia.common.security.SecurityUtil;
import vn.socialmedia.dto.request.CreateConversationRequest;
import vn.socialmedia.dto.response.ConversationResponse;
import vn.socialmedia.dto.response.MessageResponse;
import vn.socialmedia.dto.response.UserSummary;
import vn.socialmedia.enums.ConversationType;
import vn.socialmedia.enums.ErrorCode;
import vn.socialmedia.exception.BusinessException;
import vn.socialmedia.model.Conversation;
import vn.socialmedia.model.ConversationParticipant;
import vn.socialmedia.model.Message;
import vn.socialmedia.model.User;
import vn.socialmedia.repository.ConversationParticipantRepository;
import vn.socialmedia.repository.ConversationRepo;
import vn.socialmedia.repository.UserRepository;
import vn.socialmedia.service.ConversationService;
import vn.socialmedia.service.OnlineStatusService;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j(topic = "CONVERSATION-SERVICE")
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {
    private final ConversationRepo conversationRepo;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private final OnlineStatusService onlineStatusService;
    private final UserRepository userRepository;

    @Override
    public Conversation getOrCreateConversation(User currentUser, User recipientUser) {

        //TODO check blocked
        Optional<Conversation> existing = conversationRepo.findDirectConversationBetweenUsers(currentUser.getId(), recipientUser.getId());

        if (existing.isPresent()) {
            return existing.get();
        }

        Conversation conversation = Conversation.builder()
                .type(ConversationType.DIRECT)
                .build();

        conversationRepo.save(conversation);

        ConversationParticipant participant1 = ConversationParticipant.builder()
                .conversation(conversation)
                .user(currentUser)
                .build();

        ConversationParticipant participant2 = ConversationParticipant.builder()
                .conversation(conversation)
                .user(recipientUser)
                .build();

        conversationParticipantRepository.save(participant1);
        conversationParticipantRepository.save(participant2);

        return conversation;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversations(Long userId, String cursor, int limit) {

        //TODO add cache
        return conversationRepo.findByUserId(userId).stream()
                .map(conversation -> toConversationResponse(conversation, userId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationResponse getConversationById(Long conversationId) {
        User currentUser = SecurityUtil.getUser();

        Conversation conversation = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONVERSATION_NOT_FOUND, conversationId));

        conversationParticipantRepository.findByConversation_IdAndUser_Id(conversationId, currentUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_PERMISSION));

        return toConversationResponse(conversation, currentUser.getId());
    }

    @Override
    @Transactional
    public ConversationResponse createConversation(CreateConversationRequest request) {
        User currentUser = SecurityUtil.getUser();

        Set<Long> targetUserIds = request.getParticipantIds().stream()
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        targetUserIds.remove(currentUser.getId());

        if (targetUserIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY);
        }

        if (targetUserIds.size() == 1) {
            Long targetId = targetUserIds.iterator().next();
            User targetUser = userRepository.findById(targetId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, targetId));
            Conversation conversation = getOrCreateConversation(currentUser, targetUser);
            return toConversationResponse(conversation, currentUser.getId());
        }

        List<User> targetUsers = userRepository.findAllById(targetUserIds);
        if (targetUsers.size() != targetUserIds.size()) {
            Set<Long> foundUserIds = targetUsers.stream().map(User::getId).collect(Collectors.toSet());
            Long missingId = targetUserIds.stream().filter(id -> !foundUserIds.contains(id)).findFirst().orElse(0L);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, missingId);
        }

        Conversation conversation = Conversation.builder()
                .type(ConversationType.GROUP)
                .build();
        conversation = conversationRepo.save(conversation);

        List<ConversationParticipant> participants = new ArrayList<>();
        participants.add(ConversationParticipant.builder()
                .conversation(conversation)
                .user(currentUser)
                .build());

        for (User targetUser : targetUsers) {
            participants.add(ConversationParticipant.builder()
                    .conversation(conversation)
                    .user(targetUser)
                    .build());
        }

        conversationParticipantRepository.saveAll(participants);
        return toConversationResponse(conversation, currentUser.getId());
    }

    private ConversationResponse toConversationResponse(Conversation conversation, Long currentUserId) {
        List<Message> messages = new ArrayList<>(conversation.getMessages());
        Message lastMsg = messages.isEmpty() ? null : messages.getLast();

        List<UserSummary> participantSummaries = conversation.getParticipants().stream()
                .map(cp -> UserSummary.builder()
                        .id(cp.getUser().getId())
                        .displayName(cp.getUser().getName())
                        .avatar(cp.getUser().getAvatar())
                        .isOnline(onlineStatusService.isOnline(cp.getUser().getUsername()))
                        .build())
                .collect(Collectors.toList());

        Integer unreadCount = conversation.getParticipants().stream()
                .filter(cp -> cp.getUser().getId().equals(currentUserId))
                .map(ConversationParticipant::getUnreadCount)
                .findFirst()
                .orElse(0);

        return ConversationResponse.builder()
                .id(conversation.getId())
                .type(conversation.getType())
                .lastMessageAt(conversation.getLastMessageAt())
                .participants(participantSummaries)
                .lastMessage(lastMsg != null ? MessageResponse.builder()
                                               .id(lastMsg.getId())
                                               .message(lastMsg.getMessage())
                                               .mediaUrl(lastMsg.getMediaUrl())
                                               .createdAt(lastMsg.getCreatedAt())
                                               .conversationId(conversation.getId())
                                               .build() : null)
                .unreadCount(unreadCount)
                .build();
    }
}
