package vn.socialmedia.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.socialmedia.dto.request.SendMessageRequest;
import vn.socialmedia.dto.response.ResponseData;
import vn.socialmedia.service.MessageService;

import java.util.List;

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
                    This API is used to send a message to another user.
                    - Supports sending text message or media (image/video)
                    - Media will be uploaded to cloud storage
                    - Message data is sent using multipart/form-data
                    """
    )
    @PostMapping(consumes = "multipart/form-data")
    public ResponseData<?> sendMessage(
            @Valid @ModelAttribute SendMessageRequest request,
            @RequestParam(required = false) List<MultipartFile> medias) {
        log.info("Request to send message to user: {}", request.getRecipientId());
        messageService.createMessage(request, medias);
        return new ResponseData<>(HttpStatus.CREATED.value(), "Message sent successfully");
    }
}
