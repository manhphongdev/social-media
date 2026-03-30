package vn.socialmedia.service;

public interface ChatViewStateService {

    void startViewingConversation(String username, Long conversationId, String sessionId);

    void stopViewingConversation(String username, Long conversationId, String sessionId);

    boolean isViewingConversation(String username, Long conversationId);

    void clearViewingStateBySession(String username, String sessionId);
}
