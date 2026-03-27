package vn.socialmedia.service;

import org.springframework.web.multipart.MultipartFile;
import vn.socialmedia.dto.request.SendMessageRequest;

import java.util.List;

public interface MessageService {
    void createMessage(SendMessageRequest request, List<MultipartFile> files);
}
