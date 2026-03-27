package vn.socialmedia.service.impl;

import jakarta.annotation.PostConstruct;
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
import vn.socialmedia.repository.ConversationRepo;
import vn.socialmedia.repository.MessageRepo;
import vn.socialmedia.repository.UserRepository;
import vn.socialmedia.service.CloudService;
import vn.socialmedia.service.ConversationService;
import vn.socialmedia.service.MessageService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "MESSAGE-SERVICE")
public class MessageServiceImpl implements MessageService {
    private final MessageRepo messageRepo;
    private final UserRepository userRepository;
    private final ConversationService conversationService;
    private final CloudService cloudService;
    private final SimpMessageSendingOperations messageTemplate;
    private final ConversationRepo conversationRepo;

    private Map<MediaType, Function<MultipartFile, String>> uploadStrategies;

    @PostConstruct
    void initUploadStrategies() {
        uploadStrategies =
                Map.of(
                        MediaType.IMAGE, file -> {
                            FileHelper.validateImage(file);
                            return cloudService.uploadImage(file, FolderName.MESSAGE_IMAGE);
                        },
                        MediaType.VIDEO, file -> {
                            FileHelper.validateVideo(file);
                            return cloudService.uploadVideo(file, FolderName.MESSAGE_VIDEO);
                        }
                );
    }

    @Override
    public void createMessage(SendMessageRequest request, List<MultipartFile> files) {
        if (request.getMessage() == null && files.isEmpty()) {
            return;
        }
        //Step1 : find or create new conversation
        User currentUser = SecurityUtil.getUser();
        User recipientUser = userRepository.findById(request.getRecipientId()).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Conversation conversation = conversationService.getOrCreateConversation(currentUser, recipientUser);

        //Step2 : create message
        String url = null;
        MediaType mediaType = MediaType.NONE;

        //TODO upload files with asynch
        conversation.setLastMessageAt(LocalDateTime.now());

        Message message = Message.builder()
                .message(request.getMessage())
                .mediaType(mediaType)
                .mediaUrl(url)
                .isRead(false)
                .conversation(conversation)
                .user(currentUser).build();
        conversation.setLastMessageAt(LocalDateTime.now()); //TODO check lastSeen
        conversationRepo.save(conversation);

        messageRepo.save(message);

        broadcastMessage(MessageResponse.builder().id(message.getId())
                        .message(message.getMessage())
                        .mediaType(mediaType)
                        .mediaUrl(url)
                        .isRead(message.getIsRead())
                        .createdAt(message.getCreatedAt())
                        .sender(UserSummary.builder()
                                .id(currentUser.getId())
                                .displayName(currentUser.getName())
                                .avatar(currentUser.getAvatar())
                                .build())
                        .conversationId(conversation.getId())
                        .build(),
                recipientUser.getUsername());
    }

    private void broadcastMessage(MessageResponse messageResponse, String recipientUsername) {
        messageTemplate.convertAndSendToUser(recipientUsername, "queue/messages", messageResponse);
    }

    private List<String> handleMessageMediaUpload(MultipartFile[] files, Message message) {
        List<String> messageMedias = new ArrayList<>();
        for (MultipartFile file : files) {
            MediaType mediaType = FileHelper.extractMediaType(file);
            messageMedias.add(processMediaUpload(file, mediaType));
        }
        return messageMedias;
    }

    private String processMediaUpload(MultipartFile file, MediaType mediaType) {
        return Optional.ofNullable(uploadStrategies.get(mediaType))
                .orElseThrow(() -> new IllegalArgumentException("Unsupported media type"))
                .apply(file);
    }

}
