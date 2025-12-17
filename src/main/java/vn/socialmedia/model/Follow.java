package vn.socialmedia.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "follows")
public class Follow {
    @EmbeddedId
    private FollowId followId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("followerId")
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("followeeId")
    private User followee;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
}
