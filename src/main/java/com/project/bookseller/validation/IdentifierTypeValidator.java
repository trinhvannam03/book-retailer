package com.project.bookseller.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IdentifierTypeValidator implements ConstraintValidator<Identifier, String> {

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private static final String PHONE_REGEX = "^(\\+?84\\s?)?\\d{9}$|^84\\s?\\d{9}$|^\\d{10}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return value.matches(EMAIL_REGEX) || value.matches(PHONE_REGEX);
    }

    public static IdentifierType resolveIdentifier(String identifier) {
        return identifier.matches(EMAIL_REGEX)
                ? IdentifierType.EMAIL
                : (identifier.matches(PHONE_REGEX)
                ? IdentifierType.PHONE
                : IdentifierType.UNKNOWN);
    }
}
