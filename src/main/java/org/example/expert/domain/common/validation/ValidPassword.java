package org.example.expert.domain.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Constraint(validatedBy = PasswordConstraintValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    String message() default "Password must be at least 8 characters long and contain both a number and an uppercase letter.";
}
