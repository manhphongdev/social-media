package vn.socialmedia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.socialmedia.enums.ConversationType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConversationResponse {
    private Long id;
    private ConversationType type;          // DIRECT, GROUP
    private LocalDateTime lastMessageAt;

    // joiner
    private List<UserSummary> participants;

    private MessageResponse lastMessage;
}
