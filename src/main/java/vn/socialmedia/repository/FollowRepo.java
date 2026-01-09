package vn.socialmedia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.socialmedia.model.Follow;
import vn.socialmedia.model.User;

import java.util.Optional;

public interface FollowRepo extends JpaRepository<Follow, Long> {
    boolean existsByFollowerAndFollowee(User follower, User followee);

    Optional<Follow> getFollowByFollowerAndFollowee(User follower, User followee);

}
