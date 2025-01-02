package com.project.bookseller.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = UniqueIdentifierValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueIdentifier {
    String message() default "This email/phone has already been used. Please try again.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
