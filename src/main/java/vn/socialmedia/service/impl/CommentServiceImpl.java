package vn.socialmedia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.socialmedia.common.security.SecurityUtil;
import vn.socialmedia.dto.request.CommentCreationRequest;
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
}
