package vn.socialmedia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.socialmedia.model.Message;

import java.util.Optional;

@Repository
public interface MessageRepo extends JpaRepository<Message, Long> {

    Optional<Message> findFirstByConversation_IdOrderByCreatedAtDescIdDesc(Long conversationId);
}
