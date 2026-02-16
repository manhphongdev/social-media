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

        if (!postService.canViewFriendOnlyPost(post) && !postService.canViewPrivatePost(post)) {
            throw new BusinessException(ErrorCode.NO_ACCESS_POST, post.getId());
        }

        Comment parent = new Comment();
        if (request.getCommentParentId() != null) {
            parent = commentRepository.findById(request.getCommentParentId()).orElseThrow(() ->
                    new BusinessException(ErrorCode.COMMENT_NOT_FOUND, request.getCommentParentId(), request.getPostId()));
        }

        Comment comment = commentMapper.toEntity(request);
        comment.setPost(post);
        comment.setParentComment(parent);
        comment.setUser(user);

        commentRepository.save(comment);
        Notification notification = Notification.builder()
                .type(NotificationType.COMMENT)
                .text(user.getName() + " replied you in the post") //TODO fix message
                .fromUser(user)
                .targetType(NotificationTargetType.USER) //TODO fix target
                .targetId(user.getId())
                .build();
        notificationService.sendToUsers(getCommentFollower(comment), notification);
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

    private List<String> getCommentFollower(Comment comment) {
        List<String> usernames;
        Comment parent = comment.getParentComment();
        User owner = comment.getUser();
        Predicate<User> notAuthor = u -> !u.equals(owner);
        //TODO add predicate blocked user

        if (parent == null) {
            usernames = comment.getPost().getComments().stream()
                    .map(Comment::getUser)
                    .filter(Objects::nonNull)
                    .filter(notAuthor)
                    .map(User::getUsername)
                    .distinct()
                    .toList();
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

    private void broadcastPublicComment(CommentResponse response) {
        //TODO check online user
        messageTemplate.convertAndSend("/topic/comments");
    }
}
