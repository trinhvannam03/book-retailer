package com.project.bookseller.dto;

import com.project.bookseller.validation.UniqueIdentifier;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class RegisterDTO {
    @NotBlank(message = "This field is required")
    @UniqueIdentifier
    @Email(message = "Please provide the right email format!")
    @Length(max = 180, message = "The email you provided exceeds the maximum length of 180!")
    private String email;
    @NotBlank(message = "This field is required")
    @Pattern(
            regexp = "^(\\s*|(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+={}\\[\\]:;\"'<>,.?/\\\\|`~\\-]).{8,})$",
            message = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character."
    )

    private String password;
    @NotBlank(message = "This field is required")
    private String confirmedPassword;
    @NotBlank(message = "This field is required")
    @Pattern(
            regexp = "^(\\s*|\\s*\\S+\\s+\\S+.*)$",
            message = "Name must contain at least two words."
    )
    private String fullName;
}
