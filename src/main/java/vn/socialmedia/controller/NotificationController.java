package vn.socialmedia.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.socialmedia.dto.response.CursorPageResponse;
import vn.socialmedia.dto.response.NotificationResponse;
import vn.socialmedia.dto.response.ResponseData;
import vn.socialmedia.service.NotificationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/")
    public ResponseData<CursorPageResponse<List<NotificationResponse>>> getAllNotification(@Valid String cursor,
                                                                                           @Min(20) int limit) {
        return null;
    }
}
