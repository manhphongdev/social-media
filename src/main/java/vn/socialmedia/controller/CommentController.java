package vn.socialmedia.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.socialmedia.dto.request.CommentCreationRequest;
import vn.socialmedia.dto.response.ResponseData;
import vn.socialmedia.service.CommentService;

@RestController
@RequestMapping("/comments")
@Slf4j(topic = "COMMENT_CONTROLLER")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/create") //checked
    public ResponseData<?> createComment(@RequestBody CommentCreationRequest request) {
        log.info("Request createComment");
        commentService.create(request);
        return new ResponseData<>(HttpStatus.CREATED.value(), "Create comment successfully");
    }
}
