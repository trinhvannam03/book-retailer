package com.project.bookseller.validation;

import com.project.bookseller.authentication.UserPrincipal;
import com.project.bookseller.service.auth.UserPrincipalService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UniqueEmailValidator implements ConstraintValidator<UniqueIdentifier, String> {
    private final UserPrincipalService userDetailsService;

    @Override
    public boolean isValid(String identifier, ConstraintValidatorContext constraintValidatorContext) {
        UserPrincipal userDetails = userDetailsService.loadUserByIdentifier(identifier);
        return userDetails == null;
    }
}
