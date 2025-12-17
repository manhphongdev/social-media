package vn.socialmedia.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity(name = "conversation_participants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationParticipants {

    @EmbeddedId
    private ConversationParticipantsId id;

    @ManyToOne()
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User userJoined;

    @ManyToOne()
    @MapsId("conversationId")
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;
}
