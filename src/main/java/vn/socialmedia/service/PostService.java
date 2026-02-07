package vn.socialmedia.service;

import org.springframework.web.multipart.MultipartFile;
import vn.socialmedia.dto.request.PostCreationRequest;
import vn.socialmedia.dto.response.CRUDPostResponse;
import vn.socialmedia.model.Post;

public interface PostService {

    void createPost(PostCreationRequest request, MultipartFile[] files);

    void deletePost(Long id);

    CRUDPostResponse getPost(Long id);

    boolean canViewPrivatePost(Post post);

    boolean canViewFriendOnlyPost(Post post);

}
