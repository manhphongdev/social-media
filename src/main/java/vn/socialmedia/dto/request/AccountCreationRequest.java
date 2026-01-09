package vn.socialmedia.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import vn.socialmedia.common.utils.fieldValidator.Birthday;
import vn.socialmedia.common.utils.fieldValidator.Email;
import vn.socialmedia.common.utils.fieldValidator.Password;
import vn.socialmedia.enums.Gender;

import java.time.LocalDate;

@Getter
public class AccountCreationRequest {

    @Email
    private String email;

    @Password
    private String password;

    @NotNull(message = "Re-password is require")
    private String confirmPassword;

    @NotBlank(message = "Name must not be blank")
    @Size(max = 155, message = "Name must be less than 255 characters")
    private String name;

    private Gender gender;

    @Birthday(min = 12, max = 100, message = "Age must be between 12 and 100")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate dateOfBirth;

}
