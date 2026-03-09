package vn.socialmedia.service;

import org.springframework.web.multipart.MultipartFile;
import vn.socialmedia.dto.request.SendMessageRequest;

public interface MessageService {
    void createMessage(SendMessageRequest request, MultipartFile media);

}
