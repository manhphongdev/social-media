package vn.socialmedia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUploadResponse {

    private String uploadId;
    private String method;
    private String uploadUrl;
    private Map<String, String> headers;
    private String objectKey;
    private String fileUrl;
    private LocalDateTime expiresAt;
    private Long maxFileSize;
}
