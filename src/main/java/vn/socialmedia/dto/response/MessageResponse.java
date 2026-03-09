package vn.socialmedia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.socialmedia.enums.MediaType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private String message;
    private MediaType mediaType;
    private String mediaUrl;
    private Boolean isRead;
    private LocalDateTime createdAt;

    private UserSummary sender;

    private Long conversationId;
}
