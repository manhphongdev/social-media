package vn.socialmedia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.socialmedia.model.Hashtag;

import java.util.Optional;

@Repository
public interface HashtagRepo extends JpaRepository<Hashtag, Long> {
    Optional<Hashtag> findByName(String name);

    @Modifying
    @Query("""
            Update Hashtag h
            set h.usageCount = h.usageCount+1
                        where h.name = :name
            """)
    int incrementUsage(@Param("name") String name);
}
