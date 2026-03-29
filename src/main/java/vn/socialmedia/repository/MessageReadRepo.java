package vn.socialmedia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.socialmedia.model.MessageRead;
import vn.socialmedia.model.MessageReadId;

import java.util.List;

@Repository
public interface MessageReadRepo extends JpaRepository<MessageRead, MessageReadId> {
    List<MessageRead> findByMessageIdInAndUserId(List<Long> messageIds, Long userId);

    @Query("""
            select mr
            from MessageRead mr
            join fetch mr.user
            where mr.message.id in :messageIds
            order by mr.readAt asc
            """)
    List<MessageRead> findByMessageIdsWithUser(@Param("messageIds") List<Long> messageIds);
}
