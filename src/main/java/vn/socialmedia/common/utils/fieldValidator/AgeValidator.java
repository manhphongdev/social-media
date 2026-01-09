package vn.socialmedia.common.utils.fieldValidator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class AgeValidator implements ConstraintValidator<Birthday, LocalDate> {
    private int minAge;
    private int maxAge;

    @Override
    public void initialize(Birthday constraintAnnotation) {
        this.minAge = constraintAnnotation.min();
        this.maxAge = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null || value.isAfter(LocalDate.now())) {
            return false;
        }

        int age = Period.between(value, LocalDate.now()).getYears();
        return age >= this.minAge && age <= this.maxAge;
    }
}
