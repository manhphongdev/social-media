package vn.socialmedia.model;

import jakarta.persistence.*;
import lombok.*;
import vn.socialmedia.enums.PostPrivates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "posts")
public class Post extends AbstractEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String caption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repost_id", nullable = false)
    private Post repost;

    @OneToMany(mappedBy = "repost", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Post> reposts = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "NVARCHAR(15)", nullable = false)
    private PostPrivates privacy;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PostMedia>  media = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reaction>  reactions = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment>  comments = new ArrayList<>();

}
