package vn.socialmedia.service;

import vn.socialmedia.dto.response.ConversationResponse;
import vn.socialmedia.model.Conversation;
import vn.socialmedia.model.User;

import java.util.List;

public interface ConversationService {

    Conversation getOrCreateConversation(User currentUser, User recipientUser);

    List<ConversationResponse> getConversations(Long userId, String cursor, int limit);
}
