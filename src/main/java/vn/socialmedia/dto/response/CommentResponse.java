package vn.socialmedia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
    private Long id;
    private String text;
    private Long parentCommentId;
    private CRUDUserResponse user;
    private int repliesTotal;
    private LocalDateTime createdAt;
}
