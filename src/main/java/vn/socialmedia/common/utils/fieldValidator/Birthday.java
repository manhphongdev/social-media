package vn.socialmedia.common.utils.fieldValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AgeValidator.class)
public @interface Birthday {
    String message() default "Invalid Birthday";

    int min() default 0;

    int max() default 150;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
