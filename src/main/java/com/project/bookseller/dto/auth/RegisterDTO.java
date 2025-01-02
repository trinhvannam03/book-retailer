package com.project.bookseller.dto.auth;

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
    private String email;
    @NotBlank(message = "This field is required")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d).{6,}$",
            message = "Password must be at least 6 " +
                    "characters long and contain at least one uppercase letter and one digit."
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
