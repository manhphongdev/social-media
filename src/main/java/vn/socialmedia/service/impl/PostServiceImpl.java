package vn.socialmedia.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.socialmedia.dto.request.PostCreationRequest;
import vn.socialmedia.enums.FolderName;
import vn.socialmedia.enums.MediaType;
import vn.socialmedia.exception.ResourceNotFoundException;
import vn.socialmedia.helpers.FileHelper;
import vn.socialmedia.model.Hashtag;
import vn.socialmedia.model.Post;
import vn.socialmedia.model.PostMedia;
import vn.socialmedia.model.User;
import vn.socialmedia.repository.PostRepo;
import vn.socialmedia.repository.UserRepository;
import vn.socialmedia.service.CloudService;
import vn.socialmedia.service.HashtagService;
import vn.socialmedia.service.PostService;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static vn.socialmedia.common.security.SecurityUtil.getUserId;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {
    private final PostRepo postRepo;
    private final UserRepository userRepo;
    private final HashtagService hashtagService;

    private final CloudService cloudService;

    @Override
    @Transactional
    public void createPost(PostCreationRequest request, List<MultipartFile> files) {

        User user = userRepo.findById(Objects.requireNonNull(getUserId())).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Set<Hashtag> normalizedTags = hashtagService.handleHashtags(request.getHashtags());


        Post post = Post.builder()
                .user(user)
                .caption(request.getText())
                .hashtags(normalizedTags)
                .privacy(request.getPrivacy())
                .build();

        Set<PostMedia> medias = handlePostMediaUpload(files, post);
        post.setMediaFiles(medias);

        postRepo.save(post);

    }

    private Set<PostMedia> handlePostMediaUpload(List<MultipartFile> files, Post post) {
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
        return switch (mediaType) {
            case IMAGE -> cloudService.uploadFile(file, FolderName.POST_IMAGE);
            case VIDEO -> cloudService.uploadFile(file, FolderName.POST_VIDEO);
        };
    }


}
