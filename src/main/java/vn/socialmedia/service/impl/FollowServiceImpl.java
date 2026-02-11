package vn.socialmedia.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.socialmedia.dto.response.CRUDUserResponse;
import vn.socialmedia.enums.ErrorCode;
import vn.socialmedia.enums.NotificationTargetType;
import vn.socialmedia.enums.NotificationType;
import vn.socialmedia.exception.BusinessException;
import vn.socialmedia.model.Follow;
import vn.socialmedia.model.Notification;
import vn.socialmedia.model.User;
import vn.socialmedia.repository.FollowRepo;
import vn.socialmedia.repository.UserRepository;
import vn.socialmedia.service.BlockService;
import vn.socialmedia.service.FollowService;
import vn.socialmedia.service.NotificationService;

import java.util.List;
import java.util.Objects;

import static vn.socialmedia.common.security.SecurityUtil.getUser;


@Service
@Slf4j(topic = "FOLLOW_SERVICE")
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {
    private final FollowRepo followRepo;
    private final UserRepository userRepository;
    private final BlockService blockService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void follow(Long targetId) {

        User user = getUser();

        //check target user is not self
        if (Objects.equals(user.getId(), targetId)) {
            throw new BusinessException(ErrorCode.CANNOT_FOLLOW_BY_MYSELF);
        }
        // check target user exist
        User targetUser = userRepository.findById(targetId).orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        // check target user block
        boolean isBlock = blockService.isBlock(user, targetUser);
        if (isBlock) {
            throw new BusinessException(ErrorCode.CANNOT_FOLLOW_USER);
        }

        boolean isFollowExists = followRepo.existsByFollowerAndFollowee(user, targetUser);
        if (isFollowExists) {
            throw new BusinessException(ErrorCode.USER_FOLLOWED_IN_PASS);
        }

        followRepo.save(Follow.builder()
                .follower(user)
                .followee(targetUser)
                .build());

        Notification notification = Notification.builder()
                .type(NotificationType.FOLLOW)
                .text(user.getName() + " followed") //TODO fix message
                .fromUser(user)
                .user(targetUser)
                .targetType(NotificationTargetType.USER)
                .targetId(user.getId())
                .build();

        notificationService.sendToUser(targetUser.getUsername(), notification);
    }

    @Override
    public List<CRUDUserResponse> getFollowers(Long userId) {
        User user = getUser();

        return user.getFollowers().stream()
                .map(follow -> CRUDUserResponse.builder()
                        .id(follow.getFollower().getId())
                        .name(follow.getFollower().getName())
                        .avatarUrl(follow.getFollower().getAvatar())
                        .build())
                .toList();
    }

    @Override
    public List<CRUDUserResponse> getFollowees(Long userId) {
        User user = getUser();

        return user.getFollowers().stream()
                .map(follow -> CRUDUserResponse.builder()
                        .id(follow.getFollowee().getId())
                        .name(follow.getFollowee().getName())
                        .avatarUrl(follow.getFollowee().getAvatar())
                        .build())
                .toList();
    }

    @Override
    public void unfollow(Long targetId) {
        User user = getUser();
        User targerUser = userRepository.findById(targetId).orElseThrow(()
                -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Follow follow = followRepo.getFollowByFollowerAndFollowee(user, targerUser)
                .orElseThrow(() -> new BusinessException(ErrorCode.FOLLOWER_NOT_FOUND));

        followRepo.delete(follow);

    }
}
