package vn.socialmedia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import vn.socialmedia.common.helpers.CursorPageHelper;
import vn.socialmedia.common.security.SecurityUtil;
import vn.socialmedia.dto.request.CommentCreationRequest;
import vn.socialmedia.dto.request.CursorPageRequest;
import vn.socialmedia.dto.response.CRUDUserResponse;
import vn.socialmedia.dto.response.CommentResponse;
import vn.socialmedia.dto.response.CursorPageResponse;
import vn.socialmedia.enums.ErrorCode;
import vn.socialmedia.enums.NotificationTargetType;
import vn.socialmedia.enums.NotificationType;
import vn.socialmedia.exception.BusinessException;
import vn.socialmedia.mapper.CommentMapper;
import vn.socialmedia.model.Comment;
import vn.socialmedia.model.Notification;
import vn.socialmedia.model.Post;
import vn.socialmedia.model.User;
import vn.socialmedia.repository.CommentRepository;
import vn.socialmedia.repository.PostRepo;
import vn.socialmedia.service.CommentService;
import vn.socialmedia.service.NotificationService;
import vn.socialmedia.service.PostService;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static vn.socialmedia.enums.ErrorCode.NO_ACCESS_POST;
import static vn.socialmedia.enums.PostPrivacy.FRIENDS_ONLY;
import static vn.socialmedia.enums.PostPrivacy.PRIVATE;

@Service
@Slf4j(topic = "COMMENT_SERVICE")
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final PostService postService;
    private final PostRepo postRepo;
    private final CommentMapper commentMapper;
    private final NotificationService notificationService;
    private final SimpMessageSendingOperations messageTemplate;
    private final CursorPageHelper cursorHelper;

    @Override
    public void create(CommentCreationRequest request) {
        User user = SecurityUtil.getUser();

        Post post = postRepo.findById(request.getPostId()).orElseThrow(() ->
                new BusinessException(ErrorCode.POST_NOT_FOUND, request.getPostId()));

        if (post.getPrivacy() == PRIVATE &&
                !postService.canViewPrivatePost(post)) {
            throw new BusinessException(NO_ACCESS_POST);
        }

        if (post.getPrivacy() == FRIENDS_ONLY &&
                !postService.canViewFriendOnlyPost(post)) {
            throw new BusinessException(NO_ACCESS_POST);
        }

        Comment parent = null;
        if (request.getCommentParentId() != null) {
            parent = commentRepository.findById(request.getCommentParentId()).orElseThrow(() ->
                    new BusinessException(ErrorCode.COMMENT_NOT_FOUND, request.getCommentParentId(), request.getPostId()));
        }

        Comment comment = commentMapper.toEntity(request);
        comment.setPost(post);
        comment.setParentComment(parent);
        comment.setUser(user);

        commentRepository.save(comment);

        List<String> receives = getNotificationCommentFollower(comment);

        //broadcast comment to user
        receives.forEach(receive -> broadcastComment(CommentResponse.builder()
                        .id(comment.getId())
                        .text(comment.getText())
                        .user(CRUDUserResponse.builder()
                                .id(user.getId())
                                .name(user.getName())
                                .avatarUrl(user.getAvatar())
                                .build())
                        .createdAt(comment.getCreatedAt())
                        .build(),
                post.getId(), receive));

        //send realtime notification
        String notifText = parent == null ? String.format("%s comment in your post", user.getName()) : String.format("%s replied to you in the post", user.getName());
        Notification notification = Notification.builder()
                .type(NotificationType.COMMENT)
                .text(notifText)
                .fromUser(user)
                .targetType(NotificationTargetType.COMMENT)
                .targetId(comment.getId())
                .build();

        notificationService.sendToUsers(receives, notification);
    }

    @Override
    public CursorPageResponse<CommentResponse> getCommentsWithCursor(long postId, String cursor, int limit) {

        Post post = postRepo.findById(postId).orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND, postId));

        CursorPageRequest request = new CursorPageRequest();
        if (cursor != null) {
            request = cursorHelper.decodeCursor(cursor);
        }

        List<Comment> comments = commentRepository.getCommentsByPostId(postId,
                request.getLastCreatedAt(),
                request.getLastId(),
                PageRequest.of(0, limit + 1));

        boolean hasNext = comments.size() > limit;
        if (hasNext) {
            comments = comments.subList(0, limit);
        }

        List<CommentResponse> responses = comments.stream().map(comment
                        -> CommentResponse.builder()
                        .id(comment.getId())
                        .text(comment.getText())
                        .user(CRUDUserResponse.builder()
                                .name(comment.getUser().getName())
                                .avatarUrl(comment.getUser().getAvatar())
                                .id(comment.getUser().getId())
                                .build())
                        .repliesTotal(comment.getReplies().size())
                        .build())
                .toList();

        String nextCursor = null;
        if (hasNext) {
            Comment lastComment = comments.getLast();
            nextCursor = cursorHelper.encodeCursor(
                    CursorPageRequest.builder()
                            .lastCreatedAt(lastComment.getCreatedAt())
                            .lastId(lastComment.getId())
                            .build());
        }

        return CursorPageResponse.<CommentResponse>builder()
                .content(responses)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    private List<String> getNotificationCommentFollower(Comment comment) {
        List<String> usernames;
        Comment parent = comment.getParentComment();
        User commentOwner = comment.getUser();
        Predicate<User> notAuthor = u -> !u.equals(commentOwner);
        //TODO add predicate blocked user

        if (parent == null) {
            usernames = comment.getPost().getComments().stream()
                    .map(Comment::getUser)
                    .filter(Objects::nonNull)
                    .filter(notAuthor)
                    .map(User::getUsername)
                    .distinct()
                    .collect(Collectors.toList());

            String postOwnerUsername = comment.getPost().getUser().getUsername();

            if (!postOwnerUsername.equals(commentOwner.getUsername())) {
                usernames.add(postOwnerUsername);
            }
        } else {
            usernames = parent.getReplies().stream()
                    .map(Comment::getUser)
                    .filter(Objects::nonNull)
                    .filter(notAuthor)
                    .map(User::getUsername)
                    .distinct()
                    .toList();
        }
        return usernames;
    }

    private void broadcastComment(CommentResponse response, Long postId, String username) {
        messageTemplate.convertAndSendToUser(username, "/topic/posts/" + postId + "/comments", response);
    }
}
