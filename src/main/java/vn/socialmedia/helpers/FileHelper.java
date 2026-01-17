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

    private static final Tika tika = new Tika();

    private static final Set<String> IMAGE_EXT = Set.of(".jpg", ".jpeg", ".png", ".bmp", ".gif");
    private static final Set<String> VIDEO_EXT = Set.of(".mp4", ".avi");

    public static void validateImage(MultipartFile file) {
        if (!isImageValid(file)) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_TYPE);
        }
    }

    public static boolean isImageValid(MultipartFile file) {
        return isFileValidWithExt(file, "image/", IMAGE_EXT);
    }

    public static boolean isVideoValid(MultipartFile file) {
        return isFileValidWithExt(file, "video/", VIDEO_EXT);
    }

    private static boolean isFileValidWithExt(MultipartFile file, String expectedMimePrefix, Set<String> validExtensions) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String mime = detectMime(file);
        if (!mime.startsWith(expectedMimePrefix)) {
            return false;
        }

        String ext = getFileExtension(file);
        return validExtensions.stream().anyMatch(ext::endsWith);
    }

    public static MediaType extractMediaType(MultipartFile file) {
        String mime = detectMime(file);

        if (mime.startsWith("image/")) return MediaType.IMAGE;
        if (mime.startsWith("video/")) return MediaType.VIDEO;

        throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
    }

    private static String detectMime(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            return tika.detect(is);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    private static String getFileExtension(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null) return "";
        return name.toLowerCase();
    }
}
