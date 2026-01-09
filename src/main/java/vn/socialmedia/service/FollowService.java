package vn.socialmedia.service;

import vn.socialmedia.dto.response.CRUDUserResponse;

import java.util.List;

public interface FollowService {

    void follow(Long targetId);

    List<CRUDUserResponse> getFollowers(Long userId);

    List<CRUDUserResponse> getFollowees(Long userId);
}
