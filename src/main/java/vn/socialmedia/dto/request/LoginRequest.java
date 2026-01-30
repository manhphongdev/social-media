package vn.socialmedia.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import vn.socialmedia.common.utils.fieldValidator.Email;
import vn.socialmedia.common.utils.fieldValidator.Password;

@Getter
@Setter
public class LoginRequest {

    @Email(message = "Invalid email format")
    @Schema(example = "user@gmail.com", defaultValue = "user1@gmail.com")
    String email;
    @Password(message = "Password must be between 8 and 20 characters")
    @Schema(example = "12345678", defaultValue = "12345678")
    String password;
}
