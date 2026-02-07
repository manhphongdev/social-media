package vn.socialmedia.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.socialmedia.enums.ReactionType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrUpdateReactionRequest {
    @NotNull(message = "post id must be not null")
    private Long postId;
    private ReactionType reactionType;
}
