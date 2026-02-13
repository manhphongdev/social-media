package vn.socialmedia.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.socialmedia.dto.response.CursorPageResponse;
import vn.socialmedia.dto.response.NotificationResponse;
import vn.socialmedia.dto.response.ResponseData;
import vn.socialmedia.service.NotificationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
@Slf4j(topic = "NOTIFICATION_CONTROLLER")
@Tag(
        name = "Notification",
        description = "APIs for managing notifications (get, mark as read)"
)
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
            summary = "Get all notifications",
            description = """
                    This API retrieves all notifications for the current user with cursor-based pagination.
                    - Returns notifications in pages
                    - Supports cursor-based pagination
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Notifications retrieved successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request parameters",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content
                    )
            }
    )
    @GetMapping
    public ResponseData<CursorPageResponse<NotificationResponse>> getNotifications(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) int limit) {
        log.info("Request to get all notifications with cursor: {} and limit: {}", cursor, limit);
        CursorPageResponse<NotificationResponse> notifications = notificationService.getNotifications(cursor, limit);
        return new ResponseData<>(HttpStatus.OK.value(), "Notifications retrieved successfully", notifications);
    }

    @Operation(
            summary = "Get unread notifications",
            description = """
                    This API retrieves only unread notifications for the current user with cursor-based pagination.
                    - Returns only unread notifications
                    - Supports cursor-based pagination
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Unread notifications retrieved successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request parameters",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content
                    )
            }
    )
    @GetMapping("/unread")
    public ResponseData<CursorPageResponse<NotificationResponse>> getUnReadNotifications(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) int limit) {
        log.info("Request to get unread notifications with cursor: {} and limit: {}", cursor, limit);
        CursorPageResponse<NotificationResponse> notifications = notificationService.getUnReadNotifications(cursor, limit);
        return new ResponseData<>(HttpStatus.OK.value(), "Unread notifications retrieved successfully", notifications);
    }

    @Operation(
            summary = "Get unread notification count",
            description = """
                    This API retrieves the count of unread notifications for the current user.
                    - Returns the total number of unread notifications
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Unread count retrieved successfully"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content
                    )
            }
    )
    @GetMapping("/unread-count")
    public ResponseData<Integer> getUnreadCount() {
        log.info("Request to get unread notification count");
        Integer count = notificationService.getUnreadCount();
        return new ResponseData<>(HttpStatus.OK.value(), "Unread count retrieved successfully", count);
    }

    @Operation(
            summary = "Mark a notification as read by id",
            description = """
                    This API marks a notification for the current user as read.
                    - Updates a unread notification to read status
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Notification marked as read successfully"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content
                    )
            }
    )
    @PutMapping("/mark-read")
    public ResponseData<?> markAsRead(@RequestParam Long id) {
        log.info("Request to mark a notification as read by id");
        notificationService.markAsRead(id);
        return new ResponseData<>(HttpStatus.OK.value(), "Notification marked as read successfully");
    }
}
