package vn.socialmedia.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.socialmedia.model.Notification;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, Long> {
    List<Notification> findByUser_IdAndIsReadIsFalse(Long userId);

    @Query("""
            select n
            from Notification n
            where n.user.id = :userId
              and (
                     :createdAt is null
                     or (
                          n.createdAt < :createdAt
                          or (n.createdAt = :createdAt and n.id < :id)
                        )
                  )
            order by n.createdAt desc, n.id desc
            """)
    List<Notification> getAll(
            @Param("userId") Long userId,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("id") Long id,
            Pageable pageable
    );

}
