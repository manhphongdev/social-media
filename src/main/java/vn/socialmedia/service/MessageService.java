package vn.socialmedia.service;

import vn.socialmedia.dto.request.MessageCreationRequest;
import vn.socialmedia.dto.response.MessageResponse;

import java.util.List;

public interface MessageService {
    MessageResponse createMessage(MessageCreationRequest request);

    List<MessageResponse> getMessages(Long conversationId);

    int countUnreadConversations(long userId);

    void markConversationSeenUpToLatest(String username, Long conversationId);
}
