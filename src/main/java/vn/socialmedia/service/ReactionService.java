package vn.socialmedia.service;


import vn.socialmedia.dto.request.CreateOrUpdateReactionRequest;
import vn.socialmedia.dto.response.CursorResponse;
import vn.socialmedia.dto.response.ReactionResponse;

public interface ReactionService {
    void createOrUpdateReaction(CreateOrUpdateReactionRequest request);

    void deleteReaction(Long postId);

    CursorResponse<ReactionResponse> getReactionList(Long postId, String cursor, int limit);
}
