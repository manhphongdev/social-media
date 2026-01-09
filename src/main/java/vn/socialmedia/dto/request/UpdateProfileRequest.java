package vn.socialmedia.dto.request;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import vn.socialmedia.common.utils.fieldValidator.Birthday;
import vn.socialmedia.enums.Gender;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    @NotBlank(message = "Name must be not blank")
    @Size(min = 2, max = 100, message = "Name must be between 2-100 characters long")
    private String name;

    @Size(max = 255, message = "Bio must be less than 255 characters long")
    private String bio;

    @Birthday(min = 12, max = 100, message = "Age must be between 12 and 100")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate dateOfBirth;

    private Gender gender;

}
