package vn.socialmedia.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.socialmedia.model.Comment;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("""
            select c
            from Comment c
            where c.post.id = :postId
              and c.isDeleted = false
              and (
                     :createdAt is null
                     or (
                          c.createdAt < :createdAt
                          or (c.createdAt = :createdAt and c.id < :id)
                        )
                  )
            order by c.createdAt desc, c.id desc
            """)
    List<Comment> getCommentsByPostId(
            @Param("postId") Long postId,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("id") Long id,
            Pageable pageable
    );
}
