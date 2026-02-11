package vn.socialmedia.service;

import vn.socialmedia.dto.response.CursorPageResponse;
import vn.socialmedia.dto.response.NotificationResponse;
import vn.socialmedia.model.Notification;

import java.util.List;

public interface NotificationService {

    void broadcastNotification(Notification notification);

    void sendToUser(String toUsername, Notification notification);

    void sendToUsers(List<String> usernames, Notification notification);

    CursorPageResponse<NotificationResponse> getNotifications(String cursor, int limit);

    List<NotificationResponse> getUnReadNotifications(Long userId);

}
