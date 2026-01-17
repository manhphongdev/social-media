package vn.socialmedia.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import vn.socialmedia.enums.PostPrivacy;

import java.util.List;

@Getter
@Setter
public class PostCreationRequest {

    @NotBlank(message = "Caption can not blank")
    @Size(min = 1, max = 3000)
    private String text;

    private PostPrivacy privacy;

    private List<@Pattern(regexp = "^#+[A-Za-z0-9_]+$", message = "Invalid hashtag")
    @Size(max = 100) String> hashtags;


}
