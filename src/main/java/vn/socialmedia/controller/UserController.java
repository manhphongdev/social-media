package vn.socialmedia.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.socialmedia.dto.response.CRUDPostResponse;
import vn.socialmedia.dto.request.UpdateProfileRequest;
import vn.socialmedia.dto.response.CRUDUserResponse;
import vn.socialmedia.dto.response.CursorPageResponse;
import vn.socialmedia.dto.response.ResponseData;
import vn.socialmedia.service.PostService;
import vn.socialmedia.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;
    PostService postService;

    @PutMapping(value = "/profile") //checked
    public ResponseData<?> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                         @Valid @RequestBody UpdateProfileRequest request) {
        log.info("Request to update profile, user {}", userDetails.getUsername());

        userService.updateProfile(request, userDetails.getUsername());
        return new ResponseData<>(HttpStatus.OK.value(), "Profile updated successfully");
    }

    @PatchMapping(value = "/avatar", consumes = "multipart/form-data")
    public ResponseData<?> updateAvatar(@AuthenticationPrincipal UserDetails userDetails, @RequestPart MultipartFile avatar) {
        log.info("Updating avatar for user {}", userDetails.getUsername());
        userService.updateAvatar(userDetails.getUsername(), avatar);
        return new ResponseData<>(HttpStatus.OK.value(), "Avatar updated successfully");
    }

    @GetMapping(value = "/profile/me")
    public ResponseData<?> getProfile(@AuthenticationPrincipal UserDetails userDetails) {

        return new ResponseData<>(HttpStatus.OK.value(), "Get profile successfully",
                userService.getProfile(userDetails.getUsername()));
    }

    @GetMapping
    public ResponseData<List<CRUDUserResponse>> getAllUsers() {
        return new ResponseData<>(HttpStatus.OK.value(), "Get all users successfully", userService.getAllUsers());
    }

    @GetMapping("/{userId}/posts")
    public ResponseData<CursorPageResponse<CRUDPostResponse>> getPostsByUser(
            @PathVariable Long userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int limit) {
        return new ResponseData<>(HttpStatus.OK.value(), "Get user posts successfully",
                postService.getPostsByUserWithCursor(userId, cursor, limit));
    }
}
