package vn.socialmedia.service;

import vn.socialmedia.dto.request.GenerateUploadUrlRequest;
import vn.socialmedia.dto.response.PresignedUploadResponse;

public interface S3ServicePresign {

    PresignedUploadResponse generateUploadUrl(
            GenerateUploadUrlRequest request);
}
