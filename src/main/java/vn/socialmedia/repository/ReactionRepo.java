package vn.socialmedia.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.socialmedia.model.Reaction;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReactionRepo extends JpaRepository<Reaction, Long> {
    Reaction findByPostIdAndUserId(Long postId, Long userId);

    @Query("""
                SELECT r
                FROM Reaction r
                WHERE r.post.id = :postId
                  AND (
                      :createdAt IS NULL OR
                      (
                        r.createdAt < :createdAt
                        OR (r.createdAt = :createdAt AND r.id < :id)
                      )
                  )
                ORDER BY r.createdAt DESC, r.id DESC
            """)
    List<Reaction> getReactionListOfPost(
            @Param("postId") Long postId,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("id") Long id,
            Pageable pageable
    );
}
