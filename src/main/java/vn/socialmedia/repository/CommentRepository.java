package vn.socialmedia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.socialmedia.model.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
}
