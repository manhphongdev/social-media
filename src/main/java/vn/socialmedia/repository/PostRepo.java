package vn.socialmedia.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.socialmedia.model.Post;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepo extends JpaRepository<Post, Long> {

    @Query("""
            select p from Post p
            where p.user.id = :userId
              and (
                :createdAt is null
                or p.createdAt < :createdAt
                or (p.createdAt = :createdAt and p.id < :lastPostId)
              )
            order by p.createdAt desc, p.id desc
            """)
    List<Post> getPosts(@Param("userId") Long userId,
                        @Param("createdAt") LocalDateTime createdAt,
                        @Param("lastPostId") Long postId,
                        Pageable pageable);
}
