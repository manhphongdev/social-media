package vn.socialmedia.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageCreationRequest {
    @NotNull
    private Long conversationId;

    @Size(max = 1000, message = "Message too long")
    private String message;

    private String mediaUrl;
}
