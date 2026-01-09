package vn.socialmedia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import vn.socialmedia.enums.FolderName;
import vn.socialmedia.security.properties.AWSProperties;
import vn.socialmedia.service.CloudService;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3ServiceImpl implements CloudService {

    private final S3Client s3Client;
    private final AWSProperties awsProperties;

    @Override
    public String uploadFile(MultipartFile file, FolderName folder) {
        String key = folder.getPath() + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .key(key)
                    .bucket(awsProperties.getS3().getBucket())
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request,
                    RequestBody.fromBytes(file.getBytes())
            );
        } catch (IOException e) {
            throw new RuntimeException("Upload File Failed, cause: " + e.getMessage());
        }
        return awsProperties.getCloudfront().getUrl() + "/" + key;
    }

    @Override
    public void deleteFile(String url) {
        try {
            String key = extractKeyFromUrl(url);
            String bucket = awsProperties.getS3().getBucket();
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            log.debug("Deleting file from S3: bucket={}, key={}", bucket, key);
            s3Client.deleteObject(deleteRequest);

            log.info("File deleted successfully: {}", key);
        } catch (S3Exception e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when deleting file {}: {}", url, e.getMessage());
            throw new RuntimeException("S3 delete failed", e);
        }
    }

    private String extractKeyFromUrl(String fileUrl) {
        // Example: https://mybucket.s3.ap-southeast-1.amazonaws.com/posts/abc.png
        int index = fileUrl.indexOf(".amazonaws.com/");
        if (index == -1) {
            throw new IllegalArgumentException("Invalid S3 URL: " + fileUrl);
        }
        return fileUrl.substring(index + ".amazonaws.com/".length());
    }
}
