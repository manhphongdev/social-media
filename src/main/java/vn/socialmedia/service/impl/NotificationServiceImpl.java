package vn.socialmedia.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import vn.socialmedia.dto.request.CursorPageRequest;
import vn.socialmedia.dto.response.CursorPageResponse;
import vn.socialmedia.dto.response.NotificationResponse;
import vn.socialmedia.model.Notification;
import vn.socialmedia.repository.NotificationRepo;
import vn.socialmedia.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;

import static vn.socialmedia.common.helpers.CursorPageHelper.decodeCursor;
import static vn.socialmedia.common.helpers.CursorPageHelper.encodeCursor;
import static vn.socialmedia.common.security.SecurityUtil.getUser;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final SimpMessageSendingOperations messagingTemplate;
    private final NotificationRepo notificationRepo;

    @Override
    public void broadcastNotification(Notification notification) {
        if (notification != null) {
            notificationRepo.save(notification);
            NotificationResponse notificationResponse = NotificationResponse.builder()
                    .targetId(notification.getTargetId())
                    .type(notification.getType())
                    .targetType(notification.getTargetType())
                    .fromUser(notification.getFromUser().getId())
                    .text(notification.getText())
                    .isRead(notification.getIsRead())
                    .build();
            messagingTemplate.convertAndSend("/topic/public/notifications", notificationResponse);
        }
    }

    @Override
    public void sendToUser(String toUsername, Notification notification) {
        //TODO check session user

        if (notification != null) {
            notificationRepo.save(notification);
            NotificationResponse notificationResponse = NotificationResponse.builder()
                    .targetId(notification.getTargetId())
                    .type(notification.getType())
                    .targetType(notification.getTargetType())
                    .fromUser(notification.getFromUser().getId())
                    .text(notification.getText())
                    .isRead(notification.getIsRead())
                    .build();
            messagingTemplate.convertAndSendToUser(toUsername, "/queue/notifications", notificationResponse);
        }
    }

    @Override
    public void sendToUsers(List<String> usernames, Notification notification) {
        //TODO check session user
        usernames.forEach(username -> sendToUser(username, notification));
    }

    @Override
    public CursorPageResponse<NotificationResponse> getNotifications(String cursor,
                                                                     int limit) {
        if (limit < 20) {
            limit = 20;
        }

        LocalDateTime lastCreatedAt = null;
        Long lastId = null;

        if (cursor != null) {
            CursorPageRequest decoded = decodeCursor(cursor);
            lastCreatedAt = decoded.getLastCreatedAt();
            lastId = decoded.getLastId();
        }

        List<Notification> notifications = notificationRepo.getAll(getUser().getId(),
                lastCreatedAt,
                lastId,
                PageRequest.of(0, limit + 1));

        boolean hasNext = notifications.size() > limit;
        if (hasNext) {
            notifications = notifications.subList(0, limit);
        }

        List<NotificationResponse> notificationResponses = notifications.stream()
                .map(notification -> NotificationResponse.builder()
                        .text(notification.getText())
                        .type(notification.getType())
                        .targetType(notification.getTargetType())
                        .targetId(notification.getTargetId())
                        .isRead(notification.getIsRead())
                        .build())
                .toList();

        String nextCursor = null;

        if (hasNext) {
            Notification notificationLast = notifications.getLast();
            nextCursor = encodeCursor(CursorPageRequest
                    .builder()
                    .lastCreatedAt(notificationLast.getCreatedAt())
                    .lastId(notificationLast.getId())
                    .build()
            );
        }
        return CursorPageResponse.<NotificationResponse>builder()
                .content(notificationResponses)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    @Override
    public List<NotificationResponse> getUnReadNotifications(Long userId) {
        notificationRepo.findByUser_IdAndIsReadIsFalse(userId);
        return List.of();
    }
}
