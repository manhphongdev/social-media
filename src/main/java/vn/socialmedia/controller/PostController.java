package vn.socialmedia.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.socialmedia.dto.request.PostCreationRequest;
import vn.socialmedia.dto.response.ResponseData;
import vn.socialmedia.service.PostService;

import java.util.List;

@RestController
@RequestMapping("/posts")
@Slf4j(topic = "POST_CONTROLLER")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public ResponseData<?> createPost(@Valid @ModelAttribute PostCreationRequest request,
                                      @RequestParam(required = false) List<MultipartFile> files) {
        log.info("Request to create post");
        postService.createPost(request, files);

        return new ResponseData<>(HttpStatus.CREATED.value(), "Post created successfully");
    }
}
