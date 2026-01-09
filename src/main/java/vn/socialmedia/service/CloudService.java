package vn.socialmedia.service;

import org.springframework.web.multipart.MultipartFile;
import vn.socialmedia.enums.FolderName;

public interface CloudService {
    String uploadFile(MultipartFile file, FolderName folder);

    void deleteFile(String url);
}
