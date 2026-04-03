package vn.socialmedia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.socialmedia.model.ConversationParticipant;
import vn.socialmedia.model.ConversationParticipantId;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, ConversationParticipantId> {

    @Query("""
            select cp.user.username
            from ConversationParticipant cp
            where cp.conversation.id = :conversationId
              and cp.user.id <> :userId
            """)
    List<String> findParticipantUsernamesExcluding(@Param("conversationId") Long conversationId,
                                                   @Param("userId") Long userId);

    @Query("""
            SELECT count(cp)
            From ConversationParticipant  cp
            where cp.user.id = :userId and cp.unreadCount > 0
            """)
    int countUnreadConversations(@Param("userId") Long userId);

    Optional<ConversationParticipant> findByConversation_IdAndUser_Id(Long conversationId, Long userId);

    @Query("""
            select cp
            from ConversationParticipant cp
            join fetch cp.user
            where cp.conversation.id = :conversationId
              and cp.user.id <> :excludedUserId
            """)
    List<ConversationParticipant> findParticipantsExcludingUser(@Param("conversationId") Long conversationId,
                                                                @Param("excludedUserId") Long excludedUserId);
}
