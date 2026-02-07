package vn.socialmedia.dto.request;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReactionCursorRequest {
    private Long postId;
    private LocalDateTime lastCreatedAt;
    private Long lastReactionId;
}
