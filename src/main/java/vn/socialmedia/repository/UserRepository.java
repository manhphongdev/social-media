package vn.socialmedia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.socialmedia.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
