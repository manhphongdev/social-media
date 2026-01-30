package vn.socialmedia.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {

    VALIDATION_ERROR(1001, "Validation Failed"),
    RESOURCE_NOT_FOUND(1002, "Resource Not Found"),
    INVALID_DATE_FORMAT(1003, "Invalid Date Format"),
    INVALID_ENUM_VALUE(1004, "Invalid Enum Value"),
    INVALID_REQUEST_BODY(1005, "Invalid Request Body"),
    ENUM_MUST_NOT_BE_NULL(1006, "Enum Must Not Be Null"),
    USER_ID_NULL(1007, "User ID is Null"),

    //Error Authentication
    Email_Already_Exist(2001, "Email already exist"),
    Password_And_Re_Password_Not_Match(2002, "Password and re-password not match"),
    REFRESH_TOKEN_NOT_FOUND_IN_COOKIE(2003, "Refresh token not exist or expired in cookie"),
    INVALID_REFRESH_TOKEN(2004, "Invalid refresh token"),
    UNAUTHENTICATED(2005, "Unauthenticated"),

    //Error Upload File
    UPLOAD_FILE_FAILED(3001, "Upload File Failed"),
    INVALID_IMAGE_TYPE(3002, "Invalid image type"),
    INVALID_FILE_TYPE(3003, "Invalid file type"),
    INVALID_POST_FILE_TYPE(3004, "Invalid type of post file"),
    FILE_EXCEED_LIMIT(3005, "File exceed limit"),
    INVALID_VIDEO_TYPE(3006, "Invalid image type"),

    //Follow Business
    CANNOT_FOLLOW_BY_MYSELF(4001, "Cannot follow by myself"),
    CANNOT_FOLLOW_USER(4002, "Cannot follow user"),
    USER_FOLLOWED_IN_PASS(4003, "User followed in pass"),
    FOLLOWER_NOT_FOUND(4004, "Follower Not Found"),

    // Post Error
    POST_NOT_FOUND(5001, "Post not found with id %d"),

    // Comment Error
    COMMENT_NOT_FOUND(6001, "Comment id %d not found in post id %d"),

    ;

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
