package vn.socialmedia.helpers;

import lombok.experimental.UtilityClass;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;
import vn.socialmedia.enums.ErrorCode;
import vn.socialmedia.enums.MediaType;
import vn.socialmedia.exception.BusinessException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

@UtilityClass
public class FileHelper {

    private static final Tika TIKA = new Tika();

    private static final long MAX_IMAGE_SIZE = 50L * 1024 * 1024;  // 50MB
    private static final long MAX_VIDEO_SIZE = 500L * 1024 * 1024; // 500MB

    private static final Set<String> IMAGE_EXT = Set.of("jpg", "jpeg", "png", "bmp", "gif");
    private static final Set<String> VIDEO_EXT = Set.of("mp4", "avi");

    public static void validateImage(MultipartFile file) {
        validateSize(file, MAX_IMAGE_SIZE);
        validate(file, "image/", IMAGE_EXT, ErrorCode.INVALID_IMAGE_TYPE);
    }

    public static void validateVideo(MultipartFile file) {
        validateSize(file, MAX_VIDEO_SIZE);
        validate(file, "video/", VIDEO_EXT, ErrorCode.INVALID_VIDEO_TYPE);
    }

    public static MediaType extractMediaType(MultipartFile file) {
        String mime = detectMime(file);

        if (mime.startsWith("image/")) return MediaType.IMAGE;
        if (mime.startsWith("video/")) return MediaType.VIDEO;

        throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
    }

    // ===================== PRIVATE =====================

    private static void validateSize(MultipartFile file, long maxSize) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
        if (file.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.FILE_EXCEED_LIMIT);
        }
    }

    private static void validate(
            MultipartFile file,
            String expectedMimePrefix,
            Set<String> validExtensions,
            ErrorCode errorCode
    ) {
        String mime = detectMime(file);
        if (!mime.startsWith(expectedMimePrefix)) {
            throw new BusinessException(errorCode);
        }

        String ext = extractExtension(file);
        if (!validExtensions.contains(ext)) {
            throw new BusinessException(errorCode);
        }
    }

    private static String detectMime(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            return TIKA.detect(is);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    private static String extractExtension(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null || !name.contains(".")) return "";
        return name.substring(name.lastIndexOf('.') + 1).toLowerCase();
    }
}
