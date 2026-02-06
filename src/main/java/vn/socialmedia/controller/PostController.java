package vn.socialmedia.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.socialmedia.dto.request.PostCreationRequest;
import vn.socialmedia.dto.response.CRUDPostResponse;
import vn.socialmedia.dto.response.ResponseData;
import vn.socialmedia.service.PostService;

@RestController
@RequestMapping("/posts")
@Slf4j(topic = "POST_CONTROLLER")
@RequiredArgsConstructor
@Tag(
        name = "Post",
        description = "APIs for managing posts (create, delete posts)" //TODO update
)
public class PostController {

    private final PostService postService;

    @Operation(
            summary = "Create a new post",
            description = """
                    This API is used to create a new post.
                    - Supports uploading multiple files (images/videos)
                    - Post data is sent using multipart/form-data
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Post created successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request data",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content
                    )
            }
    )
    @PostMapping(value = "/create", consumes = "multipart/form-data") //checked
    public ResponseData<?> createPost(@Valid @ModelAttribute PostCreationRequest request,
                                      @RequestParam(required = false, name = "files") MultipartFile[] files) {
        log.info("Request to create post");
        postService.createPost(request, files);
        //TODO must be update(extract hashtag from content)
        return new ResponseData<>(HttpStatus.CREATED.value(), "Post created successfully");
    }

    @Operation(
            summary = "Delete a post",
            description = "Delete a post by its ID (soft delete)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Post deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "1002",
                            description = "Post not found",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content
                    )
            }
    )
    @DeleteMapping("/") //checked
    public ResponseData<?> deletePost(@RequestParam long id) {
        log.info("Request to delete post by id {}", id);
        postService.deletePost(id);

        return new ResponseData<>(HttpStatus.OK.value(), "Post deleted successfully");
    }

    @GetMapping("/{id}")
    public ResponseData<?> getPost(@PathVariable long id) {
        log.info("Request to get post by id {}", id);
        CRUDPostResponse post = postService.getPost(id);
        return new ResponseData<>(HttpStatus.OK.value(), "Post found successfully", post);
    }

}
