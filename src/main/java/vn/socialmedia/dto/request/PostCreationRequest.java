package vn.socialmedia.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import vn.socialmedia.enums.PostPrivacy;

@Getter
@Setter
public class PostCreationRequest {

    @NotBlank(message = "Caption can not blank")
    @Size(min = 1, max = 3000)
    private String text;

    private PostPrivacy privacy;

}
