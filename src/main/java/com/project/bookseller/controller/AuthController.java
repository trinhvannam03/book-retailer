package com.project.bookseller.controller;

import com.project.bookseller.authentication.AuthenticationResponseDTO;
import com.project.bookseller.authentication.UserPrincipal;
import com.project.bookseller.dto.auth.AuthDTO;
import com.project.bookseller.dto.auth.RegisterDTO;
import com.project.bookseller.entity.user.Session;
import com.project.bookseller.exceptions.BadCredentialsException;
import com.project.bookseller.exceptions.InvalidTokenException;
import com.project.bookseller.exceptions.PassWordNotMatch;
import com.project.bookseller.exceptions.UniqueColumnViolationException;
import com.project.bookseller.service.auth.TokenService;
import com.project.bookseller.service.auth.UserPrincipalService;
import com.project.bookseller.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/")
@RequiredArgsConstructor
public class AuthController {
    private final UserPrincipalService userPrincipalService;
    private final TokenService tokenService;
    private final UserService userService;

    @PostMapping("/validate_email")
    public ResponseEntity<Map<String, Object>> validateEmail(@RequestBody(required = false) RegisterDTO info) {
        String email = info.getEmail();
        Map<String, Object> response = new HashMap<>();
        if (email != null && !email.isEmpty()) {
            UserPrincipal userPrincipal = userPrincipalService.loadUserByIdentifier(email);
            if (userPrincipal != null) {
                response.put("email", "Email already exists!");
            }
        }
        return ResponseEntity.ok(response);
    }

    //refresh token, create new tokens using sessionId, extend session
    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponseDTO> refreshToken(@RequestBody AuthenticationResponseDTO authenticationResponseDTO) throws InvalidTokenException {
        String refreshToken = authenticationResponseDTO.getRefreshToken();
        System.out.println(refreshToken);
        try {
            AuthenticationResponseDTO response = tokenService.refreshTokens(refreshToken, authenticationResponseDTO.getSession());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }

    //login, create session
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDTO> login(@Valid @RequestBody AuthDTO info) throws InvalidTokenException {
        try {
            AuthenticationResponseDTO authenticationResponseDTO = userService.login(info);
            return new ResponseEntity<>(authenticationResponseDTO, HttpStatus.OK);
        } catch (BadCredentialsException e) {
            throw new RuntimeException(e);
        }
    }

    //register
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponseDTO> register(@Valid @RequestBody RegisterDTO info) throws PassWordNotMatch {
        try {
            AuthenticationResponseDTO authenticationResponseDTO = userService.register(info);
            return new ResponseEntity<>(authenticationResponseDTO, HttpStatus.OK);
        } catch (UniqueColumnViolationException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        }
    }

    //logout, set INACTIVE session
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@AuthenticationPrincipal UserPrincipal userDetails, @RequestBody Session session) {
        Map<String, String> map = userService.logout(userDetails, session);
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @PostMapping("/oauth2")
    public ResponseEntity<AuthenticationResponseDTO> oauth2(@RequestBody Map<String, String> payload) throws InvalidTokenException {
        if (payload == null || payload.get("code").isBlank()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String code = payload.get("code");
        AuthenticationResponseDTO authenticationResponseDTO = userService.oauth2Login(code);
        return new ResponseEntity<>(authenticationResponseDTO, HttpStatus.OK);
    }
}
