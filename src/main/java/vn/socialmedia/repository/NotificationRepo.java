package vn.socialmedia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.socialmedia.model.Notification;

import java.util.List;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, Long> {
    List<Notification> findByUser_Id(Long userId);


    List<Notification> findByUser_IdAndIsReadIsFalse(Long userId);
}
