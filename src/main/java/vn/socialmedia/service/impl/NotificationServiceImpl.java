package vn.socialmedia.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import vn.socialmedia.dto.response.NotificationResponse;
import vn.socialmedia.model.Notification;
import vn.socialmedia.repository.NotificationRepo;
import vn.socialmedia.service.NotificationService;

import java.util.List;

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
        //TODO save db
        usernames.forEach(username -> sendToUser(username, notification));
    }

    @Override
    public List<NotificationResponse> getNotifications(Long userId) {

        return notificationRepo.findByUser_Id(userId)
                .stream()
                .map(notification -> NotificationResponse
                        .builder()
                        .build())
                .toList();
    }

    @Override
    public List<NotificationResponse> getUnReadNotifications(Long userId) {
        notificationRepo.findByUser_IdAndIsReadIsFalse(userId);
        return List.of();
    }
}
