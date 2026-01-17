package vn.socialmedia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.socialmedia.model.Post;

@Repository
public interface PostRepo extends JpaRepository<Post, Long> {
}
