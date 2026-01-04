package vn.socialmedia.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import vn.socialmedia.enums.ErrorCode;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String customMessage;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = null;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.customMessage = message;
    }

    public BusinessException(ErrorCode errorCode, String message, Object... arg) {
        super(String.format(message, arg));
        this.errorCode = errorCode;
        this.customMessage = String.format(message, arg);
    }

    @Override
    public String getMessage() {
        return customMessage != null ? customMessage : errorCode.getMessage();
    }
}
