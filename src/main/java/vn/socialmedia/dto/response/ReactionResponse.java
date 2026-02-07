package vn.socialmedia.dto.response;

import lombok.*;
import vn.socialmedia.enums.ReactionType;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReactionResponse {
    private CRUDUserResponse user;
    private ReactionType type;
    private Long reactionId;
    private LocalDateTime createdAt;
}
