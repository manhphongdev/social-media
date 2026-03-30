package vn.socialmedia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private String message;
    private String mediaUrl;
    private Boolean isRead;
    private LocalDateTime createdAt;

    private UserSummary sender;

    private Long conversationId;
}
