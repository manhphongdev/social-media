package vn.socialmedia.service;

import vn.socialmedia.dto.request.CommentCreationRequest;

public interface CommentService {
    void create(CommentCreationRequest request);
}
