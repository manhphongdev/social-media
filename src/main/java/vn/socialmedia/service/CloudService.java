package vn.socialmedia.service;

import org.springframework.web.multipart.MultipartFile;
import vn.socialmedia.enums.FolderName;

public interface CloudService {
    String uploadImage(MultipartFile file, FolderName folder);

    String uploadVideo(MultipartFile file, FolderName folder);

    void deleteFile(String url);
}
