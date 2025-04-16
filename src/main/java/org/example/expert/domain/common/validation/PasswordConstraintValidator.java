package org.example.expert.domain.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context){
        if(password == null) return false;
        return password.length() >= 8 &&
                password.matches(".*\\d.*") &&
                password.matches(".*[A-X].*");
    }
}
