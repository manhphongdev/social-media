package vn.socialmedia.service.validator;

import org.springframework.validation.Errors;
import vn.socialmedia.dto.request.CommentCreationRequest;

/**
 * Semantic validator for CommentCreationRequest.
 * Validates business rules that may require IO (e.g. checks against posts).
 */
public interface CommentCreationSemanticValidator {
    void validate(CommentCreationRequest request, Errors errors);
}
