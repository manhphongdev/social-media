package vn.socialmedia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import vn.socialmedia.common.security.SecurityUtil;
import vn.socialmedia.dto.request.CreateOrUpdateReactionRequest;
import vn.socialmedia.dto.request.CursorPageRequest;
import vn.socialmedia.dto.response.CRUDUserResponse;
import vn.socialmedia.dto.response.CursorPageResponse;
import vn.socialmedia.dto.response.ReactionResponse;
import vn.socialmedia.enums.ErrorCode;
import vn.socialmedia.enums.NotificationTargetType;
import vn.socialmedia.enums.NotificationType;
import vn.socialmedia.enums.ReactionType;
import vn.socialmedia.exception.BusinessException;
import vn.socialmedia.model.Notification;
import vn.socialmedia.model.Post;
import vn.socialmedia.model.Reaction;
import vn.socialmedia.model.User;
import vn.socialmedia.repository.PostRepo;
import vn.socialmedia.repository.ReactionRepo;
import vn.socialmedia.service.NotificationService;
import vn.socialmedia.service.PostService;
import vn.socialmedia.service.ReactionService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static vn.socialmedia.common.helpers.CursorPageHelper.decodeCursor;
import static vn.socialmedia.common.helpers.CursorPageHelper.encodeCursor;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "REACTION_SERVICE")
public class ReactionServiceImpl implements ReactionService {
    private final ReactionRepo reactionRepo;
    private final PostRepo postRepo;
    private final PostService postService;
    private final NotificationService notificationService;

    @Override
    public void createOrUpdateReaction(CreateOrUpdateReactionRequest request) {
        Post post = postRepo.findById(request.getPostId()).orElseThrow(() ->
                new BusinessException(ErrorCode.POST_NOT_FOUND, request.getPostId()));

        User user = SecurityUtil.getUser();

        if (!postService.canViewFriendOnlyPost(post) && !postService.canViewPrivatePost(post)) {
            throw new BusinessException(ErrorCode.NO_ACCESS_POST, post.getId());
        }

        Reaction reaction = reactionRepo.findByPostIdAndUserId(request.getPostId(), user.getId());
        if (reaction == null) {
            reaction = Reaction.builder()
                    .post(post)
                    .user(user)
                    .type(request.getReactionType())
                    .build();
        } else {
            reaction.setType(request.getReactionType());
        }
        reactionRepo.save(reaction);

        if (Objects.equals(user.getId(), post.getUser().getId())) {
            return;
        }

        Notification notification = Notification.builder()
                .fromUser(user)
                .user(post.getUser())
                .type(NotificationType.REACTION)
                .text(user.getName() + " " + getMessageFollowType(request.getReactionType()) + " the post")
                .targetType(NotificationTargetType.POST)
                .targetId(post.getId())
                .build();

        notificationService.sendToUser(user.getUsername(), notification);
    }

    @Override
    public void deleteReaction(Long postId) {
        User user = SecurityUtil.getUser();

        Reaction reaction = reactionRepo.findByPostIdAndUserId(postId, user.getId());

        if (reaction == null) {
            throw new BusinessException(ErrorCode.REACTION_NOT_FOUND, postId, user.getId());
        }

        reactionRepo.delete(reaction);
    }

    @Override
    public CursorPageResponse<ReactionResponse> getReactionList(
            Long postId,
            String cursor,
            int limit
    ) {

        if (limit < 20) {
            limit = 20;
        }

        LocalDateTime lastCreatedAt = null;
        Long lastReactionId = null;

        if (cursor != null) {
            CursorPageRequest decoded = decodeCursor(cursor);
            lastCreatedAt = decoded.getLastCreatedAt();
            lastReactionId = decoded.getLastId();
        }

        List<Reaction> reactions = reactionRepo.getReactionListOfPost(
                postId,
                lastCreatedAt,
                lastReactionId,
                PageRequest.of(0, limit + 1)
        );

        boolean hasNext = reactions.size() > limit;
        if (hasNext) {
            reactions = reactions.subList(0, limit);
        }

        List<ReactionResponse> content = reactions.stream()
                .map(r -> ReactionResponse.builder()
                        .type(r.getType())
                        .user(CRUDUserResponse.builder()
                                .id(r.getUser().getId())
                                .name(r.getUser().getName())
                                .avatarUrl(r.getUser().getAvatar())
                                .build())
                        .build())
                .toList();

        String nextCursor = null;
        if (hasNext && !reactions.isEmpty()) {
            Reaction last = reactions.getLast();
            nextCursor = encodeCursor(
                    CursorPageRequest.builder()
                            .lastCreatedAt(last.getCreatedAt())
                            .lastId(last.getId())
                            .build()
            );
        }

        return CursorPageResponse.<ReactionResponse>builder()
                .content(content)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }

    private String getMessageFollowType(ReactionType type) {
        return switch (type) {
            case LIKE -> "liked";
            case LOVE -> "loved";
            case HAHA -> "laughed";
            case ANGRY -> "reacted angrily";
            case SAD -> "reacted sadly";
            case WOW -> "wowed";
        };
    }
}
