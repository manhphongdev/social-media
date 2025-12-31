package vn.socialmedia.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import vn.socialmedia.utils.fieldValidator.Email;
import vn.socialmedia.utils.fieldValidator.Password;

@Getter
public class LoginRequest {

    @Email(message = "Invalid email format")
    @Schema(example = "user@gmail.com", defaultValue = "user1@gmail.com")
    String email;
    @Password(message = "Password must be between 8 and 20 characters")
    @Schema(example = "11111111", defaultValue = "11111111")
    String password;
}
