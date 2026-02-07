package vn.socialmedia.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import vn.socialmedia.common.security.SecurityUtil;
import vn.socialmedia.dto.request.CreateOrUpdateReactionRequest;
import vn.socialmedia.dto.request.ReactionCursorRequest;
import vn.socialmedia.dto.response.CRUDUserResponse;
import vn.socialmedia.dto.response.CursorResponse;
import vn.socialmedia.dto.response.ReactionResponse;
import vn.socialmedia.enums.ErrorCode;
import vn.socialmedia.exception.BusinessException;
import vn.socialmedia.model.Post;
import vn.socialmedia.model.Reaction;
import vn.socialmedia.model.User;
import vn.socialmedia.repository.PostRepo;
import vn.socialmedia.repository.ReactionRepo;
import vn.socialmedia.service.ReactionService;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "REACTION_SERVICE")
public class ReactionServiceImpl implements ReactionService {
    private final ReactionRepo reactionRepo;
    private final PostRepo postRepo;
    private final ObjectMapper objectMapper;

    @Override
    public void createOrUpdateReaction(CreateOrUpdateReactionRequest request) {
        Post post = postRepo.findById(request.getPostId()).orElseThrow(() ->
                new BusinessException(ErrorCode.POST_NOT_FOUND, request.getPostId()));

        User user = SecurityUtil.getUser();

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
        //TODO send notification
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
    public CursorResponse<ReactionResponse> getReactionList(
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
            ReactionCursorRequest decoded = decodeCursor(cursor);
            lastCreatedAt = decoded.getLastCreatedAt();
            lastReactionId = decoded.getLastReactionId();
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
                    ReactionCursorRequest.builder()
                            .lastCreatedAt(last.getCreatedAt())
                            .lastReactionId(last.getId())
                            .build()
            );
        }

        return CursorResponse.<ReactionResponse>builder()
                .content(content)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }


    public String encodeCursor(ReactionCursorRequest cursor) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(cursor);
            return Base64.getUrlEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Encode cursor failed", e);
        }
    }

    private ReactionCursorRequest decodeCursor(String cursor) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(cursor);
            return objectMapper.readValue(bytes, ReactionCursorRequest.class);
        } catch (Exception e) {
            throw new RuntimeException("Decode cursor failed", e);
        }
    }
}
