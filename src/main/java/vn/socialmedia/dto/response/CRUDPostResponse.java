package vn.socialmedia.dto.response;

import lombok.*;
import vn.socialmedia.enums.PostPrivacy;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CRUDPostResponse {
    Long postId;
    private PostPrivacy privacy;
    private CRUDUserResponse author;
    private String caption;
    private int reactionCount;
    private int commentCount;
    private List<String> mediaUrl;
    private LocalDateTime createdAt;
}
