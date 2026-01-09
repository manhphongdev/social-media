package vn.socialmedia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.socialmedia.model.Block;
import vn.socialmedia.model.User;

public interface BlockRepo extends JpaRepository<Block, Long> {
    boolean existsByBlockerAndBlocked(User blocker, User blocked);
}
