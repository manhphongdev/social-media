package vn.socialmedia.model;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CommentReactionId implements Serializable {
    private Long user;
    private Long comment;
}