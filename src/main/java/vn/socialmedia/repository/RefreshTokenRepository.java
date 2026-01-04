package vn.socialmedia.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.socialmedia.model.RefreshTokenBlackList;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenBlackList, String> {

    @Modifying
    @Transactional
    @Query("""
            delete from RefreshTokenBlackList r 
                        where r.expiresAt < current timestamp 
            """)
    void deleteExpiredRefreshTokens();

}
