package vn.socialmedia.service;

import org.springframework.web.multipart.MultipartFile;
import vn.socialmedia.dto.request.UpdateProfileRequest;

public interface UserService {

    void updateProfile(UpdateProfileRequest updateProfileRequest, String email);

    void updateAvatar(String email, MultipartFile avatar);

    
}
