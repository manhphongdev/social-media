package vn.socialmedia.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import vn.socialmedia.enums.ErrorCode;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode code;
    private final HttpStatus status;

    public BusinessException(ErrorCode errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.code = errorCode;
        this.status = httpStatus;
    }
}
