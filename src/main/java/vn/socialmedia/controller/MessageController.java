package vn.socialmedia.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.socialmedia.dto.request.MessageCreationRequest;
import vn.socialmedia.dto.response.ResponseData;
import vn.socialmedia.service.MessageService;

@RestController
@RequestMapping("/messages")
@Slf4j(topic = "MESSAGE_CONTROLLER")
@RequiredArgsConstructor
@Tag(
        name = "Message",
        description = "APIs for sending and managing messages"
)
public class MessageController {

    private final MessageService messageService;

    @Operation(
            summary = "Send a message",
            description = """
                    This API is used to send a message to an existing conversation.
                    - Supports sending text message or media (image/video)
                    - Media will be uploaded to cloud storage
                    - Message data is sent using multipart/form-data
                    """
    )
    @PostMapping()
    public ResponseData<?> sendMessage(
            @Valid @ModelAttribute MessageCreationRequest request) {
        log.info("Request to send message to conversation: {}", request.getConversationId());
        messageService.createMessage(request);
        return new ResponseData<>(HttpStatus.CREATED.value(), "Message sent successfully");
    }
}
