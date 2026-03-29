package vn.socialmedia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.socialmedia.model.Message;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepo extends JpaRepository<Message, Long> {
    boolean existsByIdAndConversationId(Long id, Long conversationId);

    @Query("""
            select m
            from Message m
            where m.conversation.id = :conversationId
              and (
                   :createdAt is null
                   or (
                        m.createdAt < :createdAt
                        or (m.createdAt = :createdAt and m.id < :id)
                      )
                  )
            order by m.createdAt desc, m.id desc
            """)
    List<Message> findConversationMessages(@Param("conversationId") Long conversationId,
                                           @Param("createdAt") LocalDateTime createdAt,
                                           @Param("id") Long id,
                                           Pageable pageable);

    @Query("""
            select m.id
            from Message m
            where m.conversation.id = :conversationId
              and m.id <= :lastReadMessageId
              and m.user.id <> :readerId
            """)
    List<Long> findReadableMessageIds(@Param("conversationId") Long conversationId,
                                      @Param("lastReadMessageId") Long lastReadMessageId,
                                      @Param("readerId") Long readerId);
}
