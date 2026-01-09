package vn.socialmedia.dto.response;

import lombok.Builder;

public record CRUDUserResponse(
        Long id,
        String name,
        String avatarUrl
) {
    @Builder
    public CRUDUserResponse {
    }
}
