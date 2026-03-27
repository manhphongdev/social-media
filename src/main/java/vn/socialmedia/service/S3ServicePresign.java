package vn.socialmedia.service;

import vn.socialmedia.dto.response.PresignedUploadResponse;
import vn.socialmedia.enums.FolderName;

public interface S3ServicePresign {

    PresignedUploadResponse generateUploadUrl(
            String contentType,
            String fileName,
            Long fileSize,
            FolderName folder);
}
