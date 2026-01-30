package vn.socialmedia.model;

import jakarta.persistence.*;
import lombok.*;
import vn.socialmedia.enums.PostPrivacy;

import java.io.Serializable;
import java.util.Set;

import static jakarta.persistence.CascadeType.*;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends AbstractEntity implements Serializable {
    @Column(columnDefinition = "TEXT")
    private String caption;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private PostPrivacy privacy;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repost_id")
    private Post originalPost;

    @OneToMany(mappedBy = "originalPost")
    private Set<Post> reposts;

    @OneToMany(mappedBy = "post", cascade = {PERSIST, MERGE, REMOVE}, orphanRemoval = true)
    private Set<PostMedia> mediaFiles;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Reaction> reactions;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "post_hashtags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )
    private Set<Hashtag> hashtags;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SavedPost> savedByUsers;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Report> reports;
}
