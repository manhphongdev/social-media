package vn.socialmedia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import vn.socialmedia.config.properties.AWSProperties;
import vn.socialmedia.dto.response.PresignedUploadResponse;
import vn.socialmedia.enums.FolderName;
import vn.socialmedia.service.CloudService;
import vn.socialmedia.service.S3ServicePresign;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Primary
public class S3ServiceImpl implements CloudService, S3ServicePresign {

    private static final Duration PRESIGN_TTL = Duration.ofMinutes(10);
    private static final Long MAX_IMAGE_SIZE = 10L * 1024 * 1024; //10MB
    private static final Long MAX_VIDEO_SIZE = 100L * 1024 * 1024; //100MB

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final AWSProperties awsProperties;

    @Override
    public String uploadImage(MultipartFile file, FolderName folder) {
        String key = folder.getPath() + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .key(key)
                    .bucket(awsProperties.getS3().getBucket())
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request,
                    RequestBody.fromBytes(file.getBytes())
            );
        } catch (IOException e) {
            throw new RuntimeException("Upload File Failed, cause: " + e.getMessage());
        }
        log.info("Upload File Successfully, url {}", awsProperties.getCloudfront().getUrl() + "/" + key);
        return awsProperties.getCloudfront().getUrl() + "/" + key;
    }

    @Override
    public String uploadVideo(MultipartFile file, FolderName folder) {
        return "";
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

    @Override
    public PresignedUploadResponse generateUploadUrl(
            String contentType,
            String fileName,
            Long fileSize,
            FolderName folder) {
        //validate contentType
        validateContentType(contentType);
        validateFileSize(contentType, fileSize);

        String safeFileName = normalizeFileName(fileName);
        String objectKey = folder.getPath() + "/" + UUID.randomUUID() + "-" + safeFileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(awsProperties.getS3().getBucket())
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(PRESIGN_TTL)
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);

        LocalDateTime expiresAt = LocalDateTime.now(ZoneOffset.UTC).plus(PRESIGN_TTL);
        Long maxFileSize = resolveMaxSizeByContentType(contentType);
        String fileUrl = awsProperties.getCloudfront().getUrl() + "/" + objectKey;

        return PresignedUploadResponse.builder()
                .uploadId(UUID.randomUUID().toString())
                .method("PUT")
                .uploadUrl(presigned.url().toString())
                .headers(Map.of("Content-Type", contentType))
                .objectKey(objectKey)
                .fileUrl(fileUrl)
                .expiresAt(expiresAt)
                .maxFileSize(maxFileSize)
                .build();
    }

    private String normalizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "upload.bin";
        }
        return fileName
                .trim()
                .replaceAll("[\\\\/\\s]+", "-")
                .replaceAll("[^a-zA-Z0-9._-]", "");
    }

    private void validateContentType(String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            throw new IllegalArgumentException("Invalid Content Type: " + contentType);
        }

        String lowerCase = contentType.toLowerCase();
        if (!lowerCase.startsWith("image/") && !lowerCase.startsWith("video/")) {
            throw new IllegalArgumentException("Invalid Content Type: " + contentType);
        }
    }

    private void validateFileSize(String contentType, Long fileSize) {

        if (fileSize == null || fileSize <= 0) {
            throw new IllegalArgumentException("Invalid file size limit: " + fileSize);
        }

        long maxSize = resolveMaxSizeByContentType(contentType);
        if (fileSize > maxSize) {
            throw new IllegalArgumentException("File size must not exceed: " + fileSize);
        }
    }

    private long resolveMaxSizeByContentType(String contentType) {
        return contentType.toLowerCase().startsWith("image/") ? MAX_IMAGE_SIZE : MAX_VIDEO_SIZE;
    }


}
