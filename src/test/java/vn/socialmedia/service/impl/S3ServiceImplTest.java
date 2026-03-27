package vn.socialmedia.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import vn.socialmedia.config.properties.AWSProperties;
import vn.socialmedia.dto.response.PresignedUploadResponse;
import vn.socialmedia.enums.FolderName;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ServiceImplTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private AWSProperties awsProperties;

    @Mock
    private PresignedPutObjectRequest presignedPutObjectRequest;

    private S3ServiceImpl s3Service;

    @BeforeEach
    void setUp() throws Exception {
        AWSProperties.S3 s3 = new AWSProperties.S3();
        s3.setBucket("test-bucket");

        AWSProperties.CloudFont cloudfront = new AWSProperties.CloudFont();
        cloudfront.setUrl("https://cdn.example.com");

        lenient().when(awsProperties.getS3()).thenReturn(s3);
        lenient().when(awsProperties.getCloudfront()).thenReturn(cloudfront);
        lenient().when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedPutObjectRequest);
        lenient().when(presignedPutObjectRequest.url()).thenReturn(new URL("https://signed-upload-url.example.com"));

        s3Service = new S3ServiceImpl(s3Client, s3Presigner, awsProperties);
    }

    @Test
    void generateUploadUrl_withValidImageRequest_returnsPresignedResponse() {
        PresignedUploadResponse response = s3Service.generateUploadUrl(
                "image/jpeg",
                "avatar.jpg",
                1024L,
                FolderName.MESSAGE_IMAGE
        );

        assertNotNull(response);
        assertNotNull(response.getUploadId());
        assertEquals("PUT", response.getMethod());
        assertEquals("https://signed-upload-url.example.com", response.getUploadUrl());
        assertEquals("image/jpeg", response.getHeaders().get("Content-Type"));
        assertEquals(10L * 1024 * 1024, response.getMaxFileSize());
        assertTrue(response.getObjectKey().startsWith("messageImage/"));
        assertTrue(response.getObjectKey().endsWith("-avatar.jpg"));
        assertTrue(response.getFileUrl().startsWith("https://cdn.example.com/messageImage/"));
        assertNotNull(response.getExpiresAt());
    }

    @Test
    void generateUploadUrl_withBlankFileName_usesDefaultFileName() {
        PresignedUploadResponse response = s3Service.generateUploadUrl(
                "video/mp4",
                "   ",
                1024L,
                FolderName.MESSAGE_VIDEO
        );

        assertTrue(response.getObjectKey().startsWith("messageVideo/"));
        assertTrue(response.getObjectKey().endsWith("-upload.bin"));
        assertEquals(100L * 1024 * 1024, response.getMaxFileSize());
    }

    @Test
    void generateUploadUrl_withInvalidContentType_throwsException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> s3Service.generateUploadUrl("application/pdf", "doc.pdf", 1024L, FolderName.MESSAGE_IMAGE)
        );

        assertTrue(ex.getMessage().contains("Invalid Content Type"));
    }

    @Test
    void generateUploadUrl_withExceededImageSize_throwsException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> s3Service.generateUploadUrl(
                        "image/png",
                        "image.png",
                        11L * 1024 * 1024,
                        FolderName.MESSAGE_IMAGE)
        );

        assertTrue(ex.getMessage().contains("File size must not exceed"));
    }
}
