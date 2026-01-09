package vn.socialmedia.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.socialmedia.dto.request.UpdateProfileRequest;
import vn.socialmedia.enums.FolderName;
import vn.socialmedia.exception.ResourceNotFoundException;
import vn.socialmedia.model.User;
import vn.socialmedia.repository.UserRepository;
import vn.socialmedia.service.CloudService;
import vn.socialmedia.service.FileValidationService;
import vn.socialmedia.service.UserService;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "USER_SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    FileValidationService validFile;
    CloudService cloudService;

    @Override
    public void updateProfile(UpdateProfileRequest request, String email) {
        User user = getUserByEmail(email);

        user.setName(request.getName());
        user.setBio(request.getBio());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());

        userRepository.save(user);
    }

    @Override
    public void updateAvatar(String email, MultipartFile avatar) {
        User user = getUserByEmail(email);

        validFile.validateImage(avatar);
        String filePath = cloudService.uploadFile(avatar, FolderName.AVATAR);

        user.setAvatar(filePath);
        userRepository.save(user);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }


}
