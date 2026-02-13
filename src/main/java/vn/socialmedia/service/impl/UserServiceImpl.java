package vn.socialmedia.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.socialmedia.dto.request.UpdateProfileRequest;
import vn.socialmedia.dto.response.CRUDUserResponse;
import vn.socialmedia.enums.ErrorCode;
import vn.socialmedia.enums.FolderName;
import vn.socialmedia.exception.BusinessException;
import vn.socialmedia.model.User;
import vn.socialmedia.repository.UserRepository;
import vn.socialmedia.service.CloudService;
import vn.socialmedia.service.UserService;

import static vn.socialmedia.common.helpers.FileHelper.validateImage;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "USER_SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    @Qualifier("cloudinaryServiceImpl")
    CloudService cloudService;

    @Override
    public void updateProfile(UpdateProfileRequest request, String username) {
        User user = getUserByUsername(username);

        user.setName(request.getName());
        user.setBio(request.getBio());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());

        userRepository.save(user);
    }

    @Override
    public void updateAvatar(String username, MultipartFile avatar) {
        User user = getUserByUsername(username);

        validateImage(avatar);
        String filePath = cloudService.uploadImage(avatar, FolderName.AVATAR);

        user.setAvatar(filePath);
        userRepository.save(user);
    }

    @Override
    public CRUDUserResponse getProfile(String username) {
        User user = getUserByUsername(username);
        return CRUDUserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .avatarUrl(user.getAvatar())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .bio(user.getBio())
                .build();
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }


}
