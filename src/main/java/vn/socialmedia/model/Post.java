package vn.socialmedia.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import vn.socialmedia.enums.PostPrivacy;

import java.io.Serializable;
import java.util.Set;

import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.PERSIST;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE posts SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Post extends AbstractEntity implements Serializable {
    @Column(columnDefinition = "TEXT")
    private String caption;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private PostPrivacy privacy;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = Boolean.FALSE;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repost_id")
    private Post originalPost;

    @OneToMany(mappedBy = "originalPost")
    private Set<Post> reposts;

    @OneToMany(mappedBy = "post", cascade = {PERSIST, MERGE})
    private Set<PostMedia> mediaFiles;

    @OneToMany(mappedBy = "post")
    private Set<Comment> comments;

    @OneToMany(mappedBy = "post")
    private Set<Reaction> reactions;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "post_hashtags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )
    private Set<Hashtag> hashtags;

    @OneToMany(mappedBy = "post")
    private Set<SavedPost> savedByUsers;

    @OneToMany(mappedBy = "post")
    private Set<Report> reports;
}
