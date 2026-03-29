package vn.socialmedia.service;

import vn.socialmedia.dto.request.MessageCreationRequest;

public interface MessageService {
    void createMessage(MessageCreationRequest request);
}
