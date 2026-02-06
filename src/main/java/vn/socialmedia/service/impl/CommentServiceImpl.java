package vn.socialmedia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.socialmedia.common.security.SecurityUtil;
import vn.socialmedia.dto.request.CommentCreationRequest;
import vn.socialmedia.enums.ErrorCode;
import vn.socialmedia.exception.BusinessException;
import vn.socialmedia.mapper.CommentMapper;
import vn.socialmedia.model.Comment;
import vn.socialmedia.model.Post;
import vn.socialmedia.model.User;
import vn.socialmedia.repository.CommentRepository;
import vn.socialmedia.repository.PostRepo;
import vn.socialmedia.service.CommentService;

@Service
@Slf4j(topic = "COMMENT_SERVICE")
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final PostRepo postRepo;
    private final CommentMapper commentMapper;

    @Override
    public void create(CommentCreationRequest request) {
        User user = SecurityUtil.getUser();

        Post post = postRepo.findById(request.getPostId()).orElseThrow(() ->
                new BusinessException(ErrorCode.POST_NOT_FOUND, request.getPostId()));

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
        //TODO add notification
    }
}
