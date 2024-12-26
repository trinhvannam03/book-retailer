package com.project.bookseller.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = IdentifierTypeValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Identifier {
    String message() default "Identifier of Invalid format detected. Must be a valid email or phone number.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}