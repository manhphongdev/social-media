package vn.socialmedia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.socialmedia.model.Message;

@Repository
public interface MessageRepo extends JpaRepository<Message, Long> {
}
