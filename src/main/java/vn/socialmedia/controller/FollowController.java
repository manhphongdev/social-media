package vn.socialmedia.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.socialmedia.dto.response.CRUDUserResponse;
import vn.socialmedia.dto.response.ResponseData;
import vn.socialmedia.service.FollowService;

import java.util.List;


@RestController
@RequestMapping("/follows")
@RequiredArgsConstructor
@Slf4j(topic = "FOLLOW_CONTROLLER")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{targetId}")
    public ResponseData<?> followUser(@PathVariable @Positive Long targetId) {

        log.info("Following user with id {}", targetId);
        followService.follow(targetId);
        log.info("Followed user with id {}", targetId);

        return new ResponseData<>(HttpStatus.OK.value(), "Followed user with id " + targetId);
    }

    @GetMapping("/follower/{longId}")
    public ResponseData<List<CRUDUserResponse>> getFollowers(@PathVariable @Positive Long longId) {
        log.info("Request get followers from userId: {}", longId);
        List<CRUDUserResponse> followers = followService.getFollowers(longId);
        return new ResponseData<>(HttpStatus.OK.value(), "Get followers successfully", followers);

    }

    @GetMapping("/followee/{longId}")
    public ResponseData<List<CRUDUserResponse>> getFollowees(@PathVariable @Positive Long longId) {
        log.info("Request get followees from userId: {}", longId);
        List<CRUDUserResponse> followees = followService.getFollowees(longId);
        return new ResponseData<>(HttpStatus.OK.value(), "Get followees successfully", followees);

    }
}
