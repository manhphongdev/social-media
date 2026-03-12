package vn.socialmedia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.socialmedia.dto.response.ConversationResponse;
import vn.socialmedia.dto.response.MessageResponse;
import vn.socialmedia.dto.response.UserSummary;
import vn.socialmedia.enums.ConversationType;
import vn.socialmedia.model.Conversation;
import vn.socialmedia.model.ConversationParticipant;
import vn.socialmedia.model.Message;
import vn.socialmedia.model.User;
import vn.socialmedia.repository.ConversationParticipantRepository;
import vn.socialmedia.repository.ConversationRepo;
import vn.socialmedia.service.ConversationService;
import vn.socialmedia.service.OnlineStatusService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j(topic = "CONVERSATION-SERVICE")
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {
    private final ConversationRepo conversationRepo;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private final OnlineStatusService onlineStatusService;

    @Override
    public Conversation getOrCreateConversation(User currentUser, User recipientUser) {
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
    public List<ConversationResponse> getConversations(Long userId) {

        //TODO add cache
        return conversationRepo.findByUserId(userId).stream()
                .map(conversation -> {
                    List<Message> messages = new ArrayList<>(conversation.getMessages());
                    Message lastMsg = messages.isEmpty() ? null : messages.getLast();

                    // Map participants
                    List<UserSummary> participantSummaries = conversation.getParticipants().stream()
                            .map(cp -> UserSummary.builder()
                                    .id(cp.getUser().getId())
                                    .displayName(cp.getUser().getName())
                                    .avatar(cp.getUser().getAvatar())
                                    .isOnline(onlineStatusService.isOnline(cp.getUser().getUsername()))
                                    .lastSeen(LocalDateTime.now()) //TODO check last seen
                                    .build())
                            .collect(Collectors.toList());

                    return ConversationResponse.builder()
                            .id(conversation.getId())
                            .type(conversation.getType())
                            .lastMessageAt(conversation.getLastMessageAt())
                            .participants(participantSummaries)
                            .lastMessage(lastMsg != null ? MessageResponse.builder()
                                    .id(lastMsg.getId())
                                    .message(lastMsg.getMessage())
                                    .mediaType(lastMsg.getMediaType())
                                    .mediaUrl(lastMsg.getMediaUrl())
                                    .createdAt(lastMsg.getCreatedAt())
                                    .conversationId(conversation.getId())
                                    .build() : null)
                            .unreadCount(0)  //TODO counter
                            .build();
                }).toList();
    }
}
