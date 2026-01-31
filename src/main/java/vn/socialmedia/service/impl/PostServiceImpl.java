package vn.socialmedia.service.impl;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.socialmedia.dto.request.PostCreationRequest;
import vn.socialmedia.dto.response.CRUDPostResponse;
import vn.socialmedia.dto.response.CRUDUserResponse;
import vn.socialmedia.enums.FolderName;
import vn.socialmedia.enums.MediaType;
import vn.socialmedia.exception.BusinessException;
import vn.socialmedia.helpers.FileHelper;
import vn.socialmedia.model.Hashtag;
import vn.socialmedia.model.Post;
import vn.socialmedia.model.PostMedia;
import vn.socialmedia.model.User;
import vn.socialmedia.repository.FollowRepo;
import vn.socialmedia.repository.PostRepo;
import vn.socialmedia.repository.UserRepository;
import vn.socialmedia.service.CloudService;
import vn.socialmedia.service.HashtagService;
import vn.socialmedia.service.PostService;

import java.util.*;
import java.util.function.Function;

import static vn.socialmedia.common.security.SecurityUtil.getUserId;
import static vn.socialmedia.enums.ErrorCode.*;
import static vn.socialmedia.enums.PostPrivacy.FRIENDS_ONLY;
import static vn.socialmedia.enums.PostPrivacy.PRIVATE;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "POST_SERVICE")
public class PostServiceImpl implements PostService {
    private final PostRepo postRepo;
    private final UserRepository userRepo;
    private final HashtagService hashtagService;
    private final CloudService cloudService;
    private final FollowRepo followRepo;

    private Map<MediaType, Function<MultipartFile, String>> uploadStrategies;

    @PostConstruct
    void initUploadStrategies() {
        uploadStrategies =
                Map.of(
                        MediaType.IMAGE, file -> {
                            FileHelper.validateImage(file);
                            return cloudService.uploadImage(file, FolderName.POST_IMAGE);
                        },
                        MediaType.VIDEO, file -> {
                            FileHelper.validateVideo(file);
                            return cloudService.uploadVideo(file, FolderName.POST_VIDEO);
                        }
                );
    }

    @Override
    @Transactional
    public void createPost(PostCreationRequest request, MultipartFile[] files) {

        User user = userRepo.findById(Objects.requireNonNull(getUserId())).orElseThrow(() -> new BusinessException(USER_NOT_FOUND, getUserId()));

        Set<Hashtag> normalizedTags = hashtagService.handleHashtags(request.getHashtags());

        Post post = Post.builder()
                .user(user)
                .caption(request.getText())
                .hashtags(normalizedTags)
                .privacy(request.getPrivacy())
                .build();
        if (files != null) {
            Set<PostMedia> medias;
            medias = handlePostMediaUpload(files, post);
            post.setMediaFiles(medias);
        }
        postRepo.save(post);
    }

    @Override
    public void deletePost(Long id) {
        Post post = postRepo.findById(id).orElseThrow(() -> new BusinessException(POST_NOT_FOUND, id));
        postRepo.delete(post);
    }

    @Override
    public CRUDPostResponse getPost(Long id) {
        Post post = postRepo.findById(id).orElseThrow(() -> new BusinessException(POST_NOT_FOUND, id));

        //case 1: privacy: private and user is owner
        if (post.getPrivacy() == PRIVATE && !canViewPrivatePost(post)) {
            throw new BusinessException(NO_ACCESS_POST);
        }

        //case 2: privacy: friend only and current user is one of followers of post owner
        if (post.getPrivacy() == FRIENDS_ONLY && !canViewFriendOnlyPost(post)) {
            throw new BusinessException(NO_ACCESS_POST);
        }
        //TODO count reactions, comments
        return CRUDPostResponse.builder()
                .postId(id)
                .privacy(post.getPrivacy())
                .author(CRUDUserResponse.builder()
                        .id(post.getUser().getId())
                        .name(post.getUser().getName())
                        .avatarUrl(post.getUser().getAvatar())
                        .build())
                .caption(post.getCaption())
                .mediaUrl(post.getMediaFiles().stream().map(PostMedia::getUrl).toList())
                .createdAt(post.getCreatedAt())
                .build();
    }

    private Set<PostMedia> handlePostMediaUpload(MultipartFile[] files, Post post) {
        Set<PostMedia> postMedias = new HashSet<>();
        for (MultipartFile file : files) {
            MediaType mediaType = FileHelper.extractMediaType(file);
            postMedias.add(PostMedia.builder()
                    .url(processMediaUpload(file, mediaType))
                    .type(mediaType)
                    .post(post)
                    .build());
        }
        return postMedias;
    }

    private String processMediaUpload(MultipartFile file, MediaType mediaType) {
        return Optional.ofNullable(uploadStrategies.get(mediaType))
                .orElseThrow(() -> new IllegalArgumentException("Unsupported media type"))
                .apply(file);
    }

    private User getCurrentUser() {
        return userRepo.findById(Objects.requireNonNull(getUserId())).orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
    }

    private boolean canViewPrivatePost(Post post) {
        return post.getUser().equals(getCurrentUser());
    }

    private boolean canViewFriendOnlyPost(Post post) {
        return post.getUser().equals(getCurrentUser())
                || followRepo.isFollower(post.getUser().getId(), getCurrentUser().getId());
    }

}
