package vn.socialmedia.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import vn.socialmedia.enums.ErrorCode;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, Object... args) {
        super(format(errorCode, args));
        this.errorCode = errorCode;
    }

    private static String format(ErrorCode errorCode, Object[] args) {
        if (args == null || args.length == 0) {
            return errorCode.getMessage();
        }
        return String.format(errorCode.getMessage(), args);
    }
}

