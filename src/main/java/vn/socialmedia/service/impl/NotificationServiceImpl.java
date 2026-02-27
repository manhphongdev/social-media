package vn.socialmedia.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import vn.socialmedia.common.helpers.CursorPageHelper;
import vn.socialmedia.dto.request.CursorPageRequest;
import vn.socialmedia.dto.response.CursorPageResponse;
import vn.socialmedia.dto.response.NotificationResponse;
import vn.socialmedia.enums.ErrorCode;
import vn.socialmedia.exception.BusinessException;
import vn.socialmedia.model.Notification;
import vn.socialmedia.model.User;
import vn.socialmedia.repository.NotificationRepo;
import vn.socialmedia.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;

import static vn.socialmedia.common.security.SecurityUtil.getUser;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final SimpMessageSendingOperations messagingTemplate;
    private final NotificationRepo notificationRepo;
    private final CursorPageHelper cursorPageHelper;

    @Override
    public void broadcastNotification(Notification notification) {
        if (notification != null) {
            notificationRepo.save(notification);
            NotificationResponse notificationResponse = NotificationResponse.builder()
                    .id(notification.getId())
                    .targetId(notification.getTargetId())
                    .type(notification.getType())
                    .targetType(notification.getTargetType())
                    .fromUser(notification.getFromUser().getId())
                    .text(notification.getText())
                    .isRead(notification.getIsRead())
                    .createdAt(notification.getCreatedAt())
                    .build();
            messagingTemplate.convertAndSend("/topic/public/notifications", notificationResponse);
        }
    }

    @Override
    public void sendToUser(String toUsername, Notification notification) {

        if (notification != null) {
            notificationRepo.save(notification);
            NotificationResponse notificationResponse = NotificationResponse.builder()
                    .id(notification.getId())
                    .targetId(notification.getTargetId())
                    .type(notification.getType())
                    .targetType(notification.getTargetType())
                    .fromUser(notification.getFromUser().getId())
                    .text(notification.getText())
                    .isRead(notification.getIsRead())
                    .createdAt(notification.getCreatedAt())
                    .build();
            messagingTemplate.convertAndSendToUser(toUsername, "/queue/notifications", notificationResponse);
        }
    }

    @Override
    public void sendToUsers(List<String> usernames, Notification notification) {
        if (notification == null || usernames == null || usernames.isEmpty()) {
            return;
        }

        // Save ONCE
        notificationRepo.save(notification);

        NotificationResponse response = NotificationResponse.builder()
                .id(notification.getId())
                .targetId(notification.getTargetId())
                .type(notification.getType())
                .targetType(notification.getTargetType())
                .fromUser(notification.getFromUser().getId())
                .text(notification.getText())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();

        // Send to all users
        usernames.forEach(username ->
                messagingTemplate.convertAndSendToUser(username, "/queue/notifications", response)
        );
    }

    @Override
    public CursorPageResponse<NotificationResponse> getNotifications(String cursor,
                                                                     int limit) {

        LocalDateTime lastCreatedAt = null;
        Long lastId = null;

        if (cursor != null) {
            CursorPageRequest decoded = cursorPageHelper.decodeCursor(cursor);
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
                        .id(notification.getId())
                        .text(notification.getText())
                        .type(notification.getType())
                        .targetType(notification.getTargetType())
                        .targetId(notification.getTargetId())
                        .isRead(notification.getIsRead())
                        .createdAt(notification.getCreatedAt())
                        .build())
                .toList();

        return getNotificationResponseCursorPageResponse(notifications, hasNext, notificationResponses);
    }

    @Override
    public CursorPageResponse<NotificationResponse> getUnReadNotifications(String cursor, int limit) {

        LocalDateTime lastCreatedAt = null;
        Long lastId = null;

        if (cursor != null) {
            CursorPageRequest decoded = cursorPageHelper.decodeCursor(cursor);
            lastCreatedAt = decoded.getLastCreatedAt();
            lastId = decoded.getLastId();
        }

        List<Notification> notifications = notificationRepo.getUnread(getUser().getId(),
                lastCreatedAt,
                lastId,
                PageRequest.of(0, limit + 1));

        boolean hasNext = notifications.size() > limit;
        if (hasNext) {
            notifications = notifications.subList(0, limit);
        }

        List<NotificationResponse> notificationResponses = notifications.stream()
                .map(notification -> NotificationResponse.builder()
                        .id(notification.getId())
                        .text(notification.getText())
                        .type(notification.getType())
                        .targetType(notification.getTargetType())
                        .targetId(notification.getTargetId())
                        .isRead(notification.getIsRead())
                        .build())
                .toList();

        return getNotificationResponseCursorPageResponse(notifications, hasNext, notificationResponses);
    }

    private CursorPageResponse<NotificationResponse> getNotificationResponseCursorPageResponse(List<Notification> notifications, boolean hasNext, List<NotificationResponse> notificationResponses) {
        String nextCursor = null;

        if (hasNext) {
            Notification notificationLast = notifications.getLast();
            nextCursor = cursorPageHelper.encodeCursor(CursorPageRequest
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
    public Integer getUnreadCount() {
        User user = getUser();
        return notificationRepo.countNotificationByUserIdAndIsReadFalse(user.getId());
    }

    @Override
    public void markAsRead(Long id) {
        Notification notification = notificationRepo.findById(id).orElseThrow(()
                -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND, id));
        if (Boolean.TRUE.equals(notification.getIsRead())) {
            return;
        }
        notification.setIsRead(true);
        notificationRepo.save(notification);
    }
}
