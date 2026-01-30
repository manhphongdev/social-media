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
        super(message != null ? message : errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = message;
    }

    public BusinessException(ErrorCode errorCode, String message, Object... args) {
        super(format(message, args, errorCode));
        this.errorCode = errorCode;
        this.customMessage = format(message, args, errorCode);
    }

    private static String format(String message, Object[] args, ErrorCode errorCode) {
        if (message == null) {
            return errorCode.getMessage();
        }
        if (args == null || args.length == 0) {
            return message;
        }
        return String.format(message, args);
    }

    @Override
    public String getMessage() {
        return customMessage != null ? customMessage : errorCode.getMessage();
    }
}
