package vn.socialmedia.dto.request;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CursorPageRequest {
    private LocalDateTime lastCreatedAt;
    private Long lastId;
}
