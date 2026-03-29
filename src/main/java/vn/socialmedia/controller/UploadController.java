package vn.socialmedia.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.socialmedia.dto.request.GenerateUploadUrlRequest;
import vn.socialmedia.dto.response.ResponseData;
import vn.socialmedia.service.S3ServicePresign;

@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
@Slf4j
public class UploadController {
    private final S3ServicePresign s3ServicePresign;

    @PostMapping("/pre-signed")
    public ResponseData<?> getPreSignUrl(@RequestBody GenerateUploadUrlRequest request) {
        return new ResponseData<>(HttpStatus.OK.value(), "Get pre-signed url successfully",
                s3ServicePresign.generateUploadUrl(request));
    }
}
