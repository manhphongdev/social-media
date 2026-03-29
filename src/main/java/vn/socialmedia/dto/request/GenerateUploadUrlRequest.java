package vn.socialmedia.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GenerateUploadUrlRequest {

    @NotBlank(message = "contentType must not be blank")
    @Schema(example = "image/jpeg", defaultValue = "image/jpeg")
    private String contentType;

    @Size(max = 255, message = "fileName too long")
    private String fileName;

    @NotNull(message = "fileSize is required")
    @Positive(message = "fileSize must be greater than 0")
    @Schema(defaultValue = "1024")
    private Long fileSize;
}
