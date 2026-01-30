package vn.socialmedia.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.socialmedia.enums.FolderName;
import vn.socialmedia.service.CloudService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestController {

    private final CloudService cloudService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public String testUploadFile(@RequestPart MultipartFile file) {
        return cloudService.uploadImage(file, FolderName.AVATAR);
    }
}
