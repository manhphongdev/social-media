package vn.socialmedia.dto.request;

import lombok.Getter;

@Getter
public class CommentCreationRequest {
    private String text;
    private Long postId;
    private Long commentParentId;
}
