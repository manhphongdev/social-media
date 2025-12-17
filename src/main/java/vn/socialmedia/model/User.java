package vn.socialmedia.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vn.socialmedia.enums.Gender;
import vn.socialmedia.enums.UserStatus;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends AbstractEntity {

    @Column(columnDefinition = "VARCHAR(255)", nullable = false, unique = true)
    private String email;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String password;

    @Column(columnDefinition = "NVARCHAR(100)", nullable = false)
    private String name;

    @Column(name = "dateOfBirth")
    private LocalDate dateOfBirth;

    @Column(columnDefinition = "NVARCHAR(10)", name = "gender")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(columnDefinition = "VARCHAR(255)", name = "bio")
    private String bio;

    @Column(columnDefinition = "VARCHAR(255)", name = "avatar")
    private String avatar;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(15)", nullable = false, name = "status")
    private UserStatus status;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Post> posts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reaction> reactions;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments;

    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Follow> followers;

    @OneToMany(mappedBy = "followee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Follow> followees;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Notification> notifications;

    @OneToMany(mappedBy = "fromUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Notification> notificationsFrom;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Report> reportsSent;

    @OneToMany(mappedBy = "handledBy", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Report> reportsHandled;

    @OneToMany(mappedBy = "userJoined", cascade = CascadeType.ALL)
    private Set<ConversationParticipants> participants;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Message> messages;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;
}
