package vn.socialmedia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.socialmedia.common.helpers.FileHelper;
import vn.socialmedia.common.security.SecurityUtil;
import vn.socialmedia.dto.request.SendMessageRequest;
import vn.socialmedia.dto.response.MessageResponse;
import vn.socialmedia.dto.response.UserSummary;
import vn.socialmedia.enums.ErrorCode;
import vn.socialmedia.enums.FolderName;
import vn.socialmedia.enums.MediaType;
import vn.socialmedia.exception.BusinessException;
import vn.socialmedia.model.Conversation;
import vn.socialmedia.model.Message;
import vn.socialmedia.model.User;
import vn.socialmedia.repository.MessageRepo;
import vn.socialmedia.repository.UserRepository;
import vn.socialmedia.service.CloudService;
import vn.socialmedia.service.ConversationService;
import vn.socialmedia.service.MessageService;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "MESSAGE-SERVICE")
public class MessageServiceImpl implements MessageService {
    private final MessageRepo messageRepo;
    private final UserRepository userRepository;
    private final ConversationService conversationService;
    private final CloudService cloudService;
    private final SimpMessageSendingOperations messageTemplate;

    @Override
    public void createMessage(SendMessageRequest request, MultipartFile media) {
        if (request.getMessage() == null && media == null) {
            return;
        }
        //Step1 : find or create new conversation
        User currentUser = SecurityUtil.getUser();
        User recipientUser = userRepository.findById(request.getRecipientId()).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Conversation conversation = conversationService.getOrCreateConversation(currentUser, recipientUser);

        //Step2 : create message
        String url = null;
        MediaType mediaType = null;
        if (media != null) {
            mediaType = FileHelper.extractMediaType(media);
            url = mediaType == MediaType.IMAGE
                    ? cloudService.uploadImage(media, FolderName.MESSAGE_IMAGE)
                    : cloudService.uploadVideo(media, FolderName.MESSAGE_VIDEO);
        }

        Message message = Message.builder()
                .message(request.getMessage())
                .mediaType(mediaType)
                .mediaUrl(url)
                .isRead(false)
                .conversation(conversation)
                .user(currentUser).build();

        messageRepo.save(message);
        broadcastMessage(MessageResponse.builder().id(message.getId())
                        .message(message.getMessage())
                        .mediaType(mediaType)
                        .mediaUrl(url)
                        .isRead(message.getIsRead())
                        .createdAt(message.getCreatedAt())
                        .sender(UserSummary.builder().id(currentUser.getId()).displayName(currentUser.getName()).avatar(currentUser.getAvatar()).build()) //TODO checkOnline, lastSeen
                        .conversationId(conversation.getId())
                        .build(),
                recipientUser.getUsername());
    }

    private void broadcastMessage(MessageResponse messageResponse, String recipientUsername) {
        messageTemplate.convertAndSendToUser(recipientUsername, "queue/messages", messageResponse);
    }

}
