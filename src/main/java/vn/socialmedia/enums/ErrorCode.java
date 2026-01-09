package vn.socialmedia.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {

    VALIDATION_ERROR(1001, "Validation Failed"),
    RESOURCE_NOT_FOUND(1002, "Resource Not Found"),
    INVALID_DATE_FORMAT(1003, "Invalid Date Format"),
    INVALID_ENUM_VALUE(1004, "Invalid Enum Value"),
    INVALID_REQUEST_BODY(1005, ""),

    //Error Authentication
    Email_Already_Exist(2001, "Email already exist"),
    Password_And_Re_Password_Not_Match(2002, "Password and re-password not match"),
    REFRESH_TOKEN_NOT_FOUND_IN_COOKIE(2003, "Refresh token not exist or expired in cookie"),
    INVALID_REFRESH_TOKEN(2004, "Invalid refresh token"),
    UNAUTHENTICATED(2005, "Unauthenticated"),


    //Error Upload File
    UPLOAD_FILE_FAILED(3001, "Upload File Failed"),
    INVALID_IMAGE_TYPE(3002, "Invalid image type"),


    ;

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
