package com.project.bookseller.validation;

import com.project.bookseller.authentication.UserDetails;
import com.project.bookseller.service.UserDetailsService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UniqueEmailValidator implements ConstraintValidator<UniqueIdentifier, String> {
    private final UserDetailsService userDetailsService;

    @Override
    public boolean isValid(String identifier, ConstraintValidatorContext constraintValidatorContext) {
        UserDetails userDetails = userDetailsService.loadUserDetailsByIdentifier(identifier);
        return userDetails == null;
    }
}
