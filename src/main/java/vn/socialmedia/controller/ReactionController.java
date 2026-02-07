package vn.socialmedia.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.socialmedia.dto.request.CreateOrUpdateReactionRequest;
import vn.socialmedia.dto.response.ResponseData;
import vn.socialmedia.service.ReactionService;

@Slf4j
@RestController
@RequestMapping("/reactions")
@RequiredArgsConstructor
public class ReactionController {
    private final ReactionService reactionService;

    @PostMapping("/create")
    public ResponseData<?> createReaction(@RequestBody @Valid CreateOrUpdateReactionRequest request) {
        log.info("Request to create reaction");
        reactionService.createOrUpdateReaction(request);
        return new ResponseData<>(HttpStatus.CREATED.value(), "Reaction created successfully");
    }

    @GetMapping
    public ResponseData<?> getReactions(
            @RequestParam Long postId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        return new ResponseData<>(HttpStatus.OK.value(), "Get list reaction by post id successful",
                reactionService.getReactionList(postId, cursor, limit));
    }

    @DeleteMapping
    public ResponseData<?> unReaction(@RequestParam Long id) {
        reactionService.deleteReaction(id);
        return new ResponseData<>(HttpStatus.OK.value(), "Un-reaction successful");
    }
}
