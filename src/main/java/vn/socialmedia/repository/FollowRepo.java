package vn.socialmedia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.socialmedia.model.Follow;
import vn.socialmedia.model.User;

public interface FollowRepo extends JpaRepository<Follow, Long> {
    boolean existsByFollowerAndFollowee(User follower, User followee);
}
