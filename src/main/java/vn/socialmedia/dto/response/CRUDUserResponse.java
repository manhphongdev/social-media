package vn.socialmedia.dto.response;

import lombok.Builder;
import vn.socialmedia.enums.Gender;

import java.time.LocalDate;

public record CRUDUserResponse(
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
