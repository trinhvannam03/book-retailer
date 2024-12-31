package com.project.bookseller.controller;

import com.project.bookseller.authentication.UserPrincipal;
import com.project.bookseller.dto.auth.AuthDTO;
import com.project.bookseller.dto.auth.RegisterDTO;
import com.project.bookseller.dto.UserDTO;
import com.project.bookseller.entity.user.Session;
import com.project.bookseller.exceptions.InvalidTokenException;
import com.project.bookseller.exceptions.PassWordNotMatch;
import com.project.bookseller.repository.UserRepository;
import com.project.bookseller.service.auth.SessionService;
import com.project.bookseller.service.auth.TokenService;
import com.project.bookseller.service.auth.UserPrincipalService;
import com.project.bookseller.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.http.auth.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/")
@RequiredArgsConstructor
public class AuthController {
    private final PasswordEncoder passwordEncoder;
    private final UserPrincipalService userDetailsService;
    private final TokenService jwtService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final SessionService sessionService;

    //check format validity asynchronously as user types in
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> realTimeValidation(@Valid @RequestBody(required = false) RegisterDTO info) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "valid information!");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //refresh token, create new tokens using sessionId, extend session
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody UserDTO userDTO) {
        String refreshToken = userDTO.getRefreshToken();
        Map<String, String> response = new HashMap<>();
        try {
            Map<String, String> tokens = jwtService.validateRefreshToken(refreshToken, userDTO.getSession());
            tokens.put("status", "successful");
            return new ResponseEntity<>(tokens, HttpStatus.OK);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    //login, create session
    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@Valid @RequestBody AuthDTO info) throws InvalidTokenException {
        try {
            UserDTO user = userService.login(info);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (InvalidCredentialsException e) {
            throw new RuntimeException(e);
        }
    }

    //register
    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterDTO info) throws PassWordNotMatch {
        UserDTO userDTO = userService.register(info);
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    //logout, set INACTIVE session
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@AuthenticationPrincipal UserPrincipal userDetails, @RequestBody Session session) {
        Map<String, String> map = userService.logout(userDetails, session);
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @PostMapping("/oauth2")
    public ResponseEntity<UserDTO> oauth2(@RequestBody Map<String, String> payload) throws InvalidTokenException {
        if (payload == null || payload.get("code").isBlank()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String code = payload.get("code");
        UserDTO userDTO = userService.oauth2Login(code);
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }
}
