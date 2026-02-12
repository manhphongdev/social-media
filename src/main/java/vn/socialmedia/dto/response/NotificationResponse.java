package vn.socialmedia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.socialmedia.enums.NotificationTargetType;
import vn.socialmedia.enums.NotificationType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    Long id;

    private String text;

    private NotificationType type;

    private NotificationTargetType targetType;

    private Long targetId;

    private Boolean isRead;

    private Long fromUser;

    private LocalDateTime createdAt;
}
