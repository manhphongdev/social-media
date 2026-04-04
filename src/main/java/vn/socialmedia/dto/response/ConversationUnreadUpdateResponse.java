package vn.socialmedia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationUnreadUpdateResponse {
    private Long conversationId;
    private int unreadCount;
    private int totalUnreadConversations;
}
