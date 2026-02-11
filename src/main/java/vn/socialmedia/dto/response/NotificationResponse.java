package vn.socialmedia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.socialmedia.enums.NotificationTargetType;
import vn.socialmedia.enums.NotificationType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private String text;

    private NotificationType type;

    private NotificationTargetType targetType;

    private Long targetId;

    private Boolean isRead = false;

    private Long fromUser;
}
