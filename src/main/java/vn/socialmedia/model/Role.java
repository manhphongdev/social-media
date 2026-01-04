package vn.socialmedia.model;


import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends AbstractEntity implements Serializable {

    @Column(unique = true, nullable = false)
    private String name;

    @Column
    private String description;

    @ManyToMany()
    private Set<Permission> permissions;

    @Column(name = "created_at")
    private LocalDateTime createAt;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<User> users = new HashSet<>();
}
