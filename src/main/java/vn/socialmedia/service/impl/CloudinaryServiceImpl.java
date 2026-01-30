package vn.socialmedia.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.socialmedia.enums.FolderName;
import vn.socialmedia.service.CloudService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Primary
public class CloudinaryServiceImpl implements CloudService {
    private final Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file, FolderName folder) {
        try {
            Map uploadResults = cloudinary.uploader().upload(
                    file.getBytes(), ObjectUtils.asMap("folder", folder.getPath(),
                            "public_id", UUID.randomUUID().toString(),
                            "resource_type", "image")
            );
            return (String) uploadResults.get("secure_url");

        } catch (IOException e) {
            throw new RuntimeException("Upload File Failed: " + e.getMessage());
        }
    }

    @Override
    public String uploadVideo(MultipartFile file, FolderName folder) {
        try (InputStream is = file.getInputStream()) {

            Map options = ObjectUtils.asMap(
                    "resource_type", "video",
                    "folder", folder.getPath(),
                    "public_id", UUID.randomUUID().toString(),
                    "chunk_size", 6_000_000 // ~6MB
            );

            Map<?, ?> result = cloudinary.uploader().uploadLarge(is, options);

            return result.get("secure_url").toString();

        } catch (Exception e) {
            throw new RuntimeException("Upload video failed", e);
        }
    }


    @Override
    public void deleteFile(String url) {
        try {
            String publicId = extractPublicId(url);

            Map<String, Object> result = cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap("resource_type", "auto")
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }

    private String extractPublicId(String url) {
        // Lấy phần sau 'upload/'
        String withoutUpload = url.substring(url.indexOf("upload/") + 7);

        // Bỏ version ví dụ 'v1700000000/'
        if (withoutUpload.matches("^v[0-9]+/.*")) {
            withoutUpload = withoutUpload.substring(withoutUpload.indexOf("/") + 1);
        }

        // Bỏ extension .jpg, .png, .webp...
        int dotIndex = withoutUpload.lastIndexOf(".");
        return (dotIndex != -1) ? withoutUpload.substring(0, dotIndex) : withoutUpload;
    }
}
