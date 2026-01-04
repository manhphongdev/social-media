package vn.socialmedia.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.socialmedia.dto.response.ErrorResponse;
import vn.socialmedia.enums.ErrorCode;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e,
            HttpServletRequest request) {

        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorResponse.FieldError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .toList();

        log.warn("Validation failed for request: {} {} - {} validation error(s)",
                request.getMethod(),
                request.getRequestURI(),
                fieldErrors.size());

        fieldErrors.forEach(error ->
                log.warn("Field '{}': {} (rejected value: {})",
                        error.getField(),
                        error.getMessage(),
                        maskSensitiveValue(error.getField(), error.getRejectedValue()))
        );

        ErrorResponse response = ErrorResponse
                .builder()
                .code(ErrorCode.VALIDATION_ERROR.getCode())
                .message(ErrorCode.VALIDATION_ERROR.getMessage())
                .fieldErrors(fieldErrors)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        response.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

    }

    private Object maskSensitiveValue(String fieldName, Object value) {
        if (value == null) return null;

        // List field must mask
        List<String> sensitiveFields = Arrays.asList(
                "password", "confirmPassword", "oldPassword", "newPassword",
                "creditCard", "cvv", "pin", "ssn", "token", "secret"
        );

        // Check case-insensitive
        boolean isSensitive = sensitiveFields.stream()
                .anyMatch(field -> fieldName.toLowerCase().contains(field.toLowerCase()));

        if (isSensitive) {
            return "***MASKED***";
        }

        return value;
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {
        log.error("BusinessException for request: code={}, path={}, message={}", ex.getErrorCode().getCode(), request.getRequestURI(), ex.getMessage());

        ErrorResponse response = new ErrorResponse(ex.getErrorCode().getCode(), ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler({InvalidCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleInvalidCredentialError(
            InvalidCredentialsException ex,
            HttpServletRequest request) {

        ErrorResponse response = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler({TokenExpiredException.class})
    public ResponseEntity<ErrorResponse> handleTokenExpiredError(
            TokenExpiredException ex,
            HttpServletRequest request) {

        ErrorResponse response = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Resource not found: {} {} - {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage());

        ErrorResponse response = new ErrorResponse(ErrorCode.RESOURCE_NOT_FOUND.getCode(), ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {


        ErrorResponse response = new ErrorResponse(
                ErrorCode.INVALID_REQUEST_BODY.getCode(),
                ex.getMessage(), request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ErrorResponse> handleDateTimeParseError(
            DateTimeParseException ex,
            HttpServletRequest request) {

        ErrorResponse response;

        response = ErrorResponse.builder()
                .code(ErrorCode.INVALID_DATE_FORMAT.getCode())
                .message(ErrorCode.INVALID_DATE_FORMAT.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}
