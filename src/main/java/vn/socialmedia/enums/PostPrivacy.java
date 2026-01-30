package vn.socialmedia.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import vn.socialmedia.exception.BusinessException;

public enum PostPrivacy {
    PUBLIC,
    PRIVATE,
    FRIENDS_ONLY;

    @JsonCreator
    public static PostPrivacy fromValue(String value) {
        if (value == null) {
            throw new BusinessException(ErrorCode.ENUM_MUST_NOT_BE_NULL);
        }
        return PostPrivacy.valueOf(value.toUpperCase());
    }
}
