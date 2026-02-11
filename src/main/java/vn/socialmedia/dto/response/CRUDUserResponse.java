package vn.socialmedia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import vn.socialmedia.enums.Gender;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CRUDUserResponse(
        String username,
        Long id,
        String name,
        String avatarUrl,
        LocalDate dateOfBirth,
        Gender gender,
        String bio
) {
    @Builder
    public CRUDUserResponse {
    }
}
