package vn.socialmedia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.socialmedia.model.Follow;
import vn.socialmedia.model.User;

import java.util.Optional;

@Repository
public interface FollowRepo extends JpaRepository<Follow, Long> {
    boolean existsByFollowerAndFollowee(User follower, User followee);

    Optional<Follow> getFollowByFollowerAndFollowee(User follower, User followee);

    @Query("""
            SELECT EXISTS (
                        SELECT 1 
                        FROM Follow f
                        WHERE f.followee.id =:followeeId
                          AND f.follower.id =:followerId
                        )
            """)
    boolean isFollower(@Param("followeeId") Long followeeId, @Param("followerId") Long followerId);
}
