package com.project.bookseller.authentication;


import com.project.bookseller.entity.user.Session;
import com.project.bookseller.entity.user.SessionStatus;
import com.project.bookseller.exceptions.InvalidTokenException;
import com.project.bookseller.service.auth.SessionService;
import com.project.bookseller.service.auth.TokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtValidationFilter extends OncePerRequestFilter {
    private final SessionService sessionService;
    private static final String BEARER_PREFIX = "Bearer ";
    private final TokenService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = null;

        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            token = bearerToken.substring(BEARER_PREFIX.length());
        }
        try {
            if (token != null) {
                Claims claims = jwtService.extractClaims(token);
                UserDetails userDetails = jwtService.validateAccessToken(token);
                JwtAuthenticationToken authentication = new JwtAuthenticationToken(userDetails);
                Session session = sessionService.getSession(userDetails.getUserId(), claims.getAudience());
                if (session != null && session.getSessionStatus() == SessionStatus.ACTIVE) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (InvalidTokenException e) {
            e.printStackTrace();
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }
}
