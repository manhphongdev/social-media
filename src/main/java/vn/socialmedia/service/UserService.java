package vn.socialmedia.service;

import org.springframework.web.multipart.MultipartFile;
import vn.socialmedia.dto.request.UpdateProfileRequest;
import vn.socialmedia.dto.response.CRUDUserResponse;

import java.util.List;

public interface UserService {

    void updateProfile(UpdateProfileRequest updateProfileRequest, String username);

    void updateAvatar(String username, MultipartFile avatar);

    CRUDUserResponse getProfile(String username);

    List<CRUDUserResponse> getAllUsers();

}
