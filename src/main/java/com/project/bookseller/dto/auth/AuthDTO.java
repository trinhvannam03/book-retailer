
package com.project.bookseller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.bookseller.validation.Identifier;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthDTO {
    @NotBlank(message = "This field is required")
    @Identifier(message = "Invalid format for email/phone")
    private String identifier;
    @NotBlank(message = "This field is required")
    private String password;
    private String browserName;
    private String osName;
    private String osVersion;
    private String deviceType;
    private String ipAddress;
    private String timestamp;
    private String userAgent;
    private String currentPassword;
    private String passwordHash;
    private String confirmedPassword;

}
