package vn.socialmedia.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CommentCreationRequest {
    @NotBlank(message = "Comment must be not blank")
    private String text;

    private Long postId;

    private Long commentParentId;
}
