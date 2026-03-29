package vn.socialmedia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.socialmedia.model.ConversationParticipant;
import vn.socialmedia.model.User;

import java.util.List;

@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {

    @Query("""
            select cp.user.username
            from ConversationParticipant cp
            where cp.conversation.id = :conversationId
              and cp.user.id <> :userId
            """)
    List<String> findParticipantUsernamesExcluding(@Param("conversationId") Long conversationId,
                                                   @Param("userId") Long userId);

    List<ConversationParticipant> findByUser(User user);
}
