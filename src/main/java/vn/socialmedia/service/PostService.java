package vn.socialmedia.service;

import org.springframework.web.multipart.MultipartFile;
import vn.socialmedia.dto.request.PostCreationRequest;

import java.util.List;

public interface PostService {

    void createPost(PostCreationRequest request, List<MultipartFile> files);

    void deletePost(Long id);
}
