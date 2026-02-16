package vn.socialmedia.service;

import vn.socialmedia.dto.request.CommentCreationRequest;
import vn.socialmedia.dto.response.CommentResponse;
import vn.socialmedia.dto.response.CursorPageResponse;

public interface CommentService {
    void create(CommentCreationRequest request);

    CursorPageResponse<CommentResponse> getCommentsWithCursor(long postId, String cursor, int limit);
}
