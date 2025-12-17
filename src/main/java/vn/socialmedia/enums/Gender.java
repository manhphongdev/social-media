package vn.socialmedia.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum Gender {
    @JsonProperty("MALE")
    MALE,
    @JsonProperty("FEMALE")
    FEMALE,
    @JsonProperty("OTHER")
    OTHER;

    @JsonCreator
    public static Gender fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Gender cannot be null");
        }
        try {
            return Gender.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Value of gender not valid: " + value + ". Value valid: MALE, FEMALE, OTHER");
        }
    }
}
