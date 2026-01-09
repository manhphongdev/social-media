package vn.socialmedia.service.impl;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.socialmedia.enums.ErrorCode;
import vn.socialmedia.exception.BusinessException;
import vn.socialmedia.service.FileValidationService;

import java.io.IOException;
import java.io.InputStream;

@Service
public class FileValidationServiceImpl implements FileValidationService {

    private final Tika tika = new Tika();

    @Override
    public void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_TYPE);
        }


        try (InputStream is = file.getInputStream()) {

            // Kiểm tra MIME
            String mime = tika.detect(is);
            if (!mime.startsWith("image/")) {
                throw new BusinessException(ErrorCode.INVALID_IMAGE_TYPE);
            }

            // Kiểm tra header magic number để chắc chắn là ảnh
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null) {
                String ext = originalFilename.toLowerCase();

                if (!(ext.endsWith(".jpg") || ext.endsWith(".jpeg") || ext.endsWith(".png")
                        || ext.endsWith(".bmp") || ext.endsWith(".gif"))) {
                    throw new BusinessException(ErrorCode.INVALID_IMAGE_TYPE);
                }
            }

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_TYPE);
        }
    }
}
