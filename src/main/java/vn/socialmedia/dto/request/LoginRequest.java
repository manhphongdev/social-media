package vn.socialmedia.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import vn.socialmedia.common.utils.fieldValidator.Password;

@Getter
@Setter
public class LoginRequest {

    @Size(min = 5, max = 30, message = "Username must be between 5 and 30 characters")
    private String username;

    @Password(message = "Password must be between 8 and 20 characters")
    @Schema(example = "12345678", defaultValue = "12345678")
    private String password;

}
