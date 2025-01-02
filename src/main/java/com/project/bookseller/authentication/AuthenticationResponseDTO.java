package com.project.bookseller.authentication;

import com.project.bookseller.entity.user.Session;
import lombok.Data;

@Data
public class AuthenticationResponseDTO {
    private String accessToken;
    private String refreshToken;
    private Session session;
}
