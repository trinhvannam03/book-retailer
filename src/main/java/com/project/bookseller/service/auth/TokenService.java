package com.project.bookseller.service.auth;


import com.project.bookseller.authentication.AuthenticationResponseDTO;
import com.project.bookseller.authentication.UserPrincipal;
import com.project.bookseller.entity.user.Session;
import com.project.bookseller.exceptions.InvalidTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TokenService {
    //use ENV VARIABLES instead
    private static final String SECRET_KEY = "eyJ0eXwewesdsuwIsSDSDJKewwjsdWIKAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9";
    private static final String ISSUER_ID = "b2f7c240-8963-4f67-a86c-7e0dbb6f217d";
    private static final int accessTokenExpiredAfter = 1000 * 60 * 5; //5 MINUTES
    private static final int refreshTokenExpiredAfter = 1000 * 60 * 2880;
    private final UserPrincipalService userDetailsService;
    private final SessionService sessionService;

    public String generateAccessToken(UserPrincipal userPrincipal, String sessionId) {
        return Jwts.builder().setSubject(userPrincipal.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiredAfter))
                .setIssuer(ISSUER_ID)
                .setAudience(sessionId)
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY)), SignatureAlgorithm.HS256).compact();
    }

    public String generateRefreshToken(UserPrincipal userDetails, String sessionId) {
        return Jwts.builder()
                .setSubject(userDetails.getEmail())
                .setIssuedAt(new Date())
                .setId(sessionId)
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiredAfter))
                .setIssuer(ISSUER_ID)
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY)), SignatureAlgorithm.HS256).compact();
    }

    public Claims extractClaims(String token) throws InvalidTokenException {
        try {
            return Jwts.parserBuilder().setSigningKey(TokenService.SECRET_KEY).build().parseClaimsJws(token).getBody();
        } catch (Exception e) {
            e.printStackTrace();
            throw new InvalidTokenException("Invalid token");
        }
    }

    public UserPrincipal validateAccessToken(String accessToken) throws ExpiredJwtException, InvalidTokenException {
        try {
            Claims claims = extractClaims(accessToken);
            String identifier = claims.getSubject();
            String issuer = claims.getIssuer();
            UserPrincipal userDetails = userDetailsService.loadUserByIdentifier(identifier);
            if (userDetails != null && issuer.equals(ISSUER_ID)) {
                return userDetails;
            }
        } catch (Exception e) {
            throw new InvalidTokenException(e.getMessage());
        }
        throw new InvalidTokenException("Invalid token!");
    }

    public AuthenticationResponseDTO refreshTokens(String currenRefreshToken, Session session) throws InvalidTokenException, ExpiredJwtException {
        try {
            Claims claims = extractClaims(currenRefreshToken);
            System.out.println(claims);
            System.out.println(currenRefreshToken);
            String identifier = claims.getSubject();
            System.out.println("here");
            System.out.println(identifier);

            UserPrincipal userDetails = userDetailsService.loadUserByIdentifier(identifier);
            AuthenticationResponseDTO authenticationResponseDTO = new AuthenticationResponseDTO();
            System.out.println(userDetails != null && claims.getExpiration().after(new Date()));
            System.out.println(userDetails != null);
            if (userDetails != null && claims.getExpiration().after(new Date())) {
                String accessToken = generateAccessToken(userDetails, session.getSessionId());
                String refreshToken = generateRefreshToken(userDetails, session.getSessionId());
                System.out.println(currenRefreshToken);
                System.out.println(accessToken);
                System.out.println(refreshToken);
                authenticationResponseDTO.setAccessToken(accessToken);
                authenticationResponseDTO.setRefreshToken(refreshToken);
                sessionService.extendSession(userDetails.getUserId(), session, session.getExpiredAt().plusHours(24));
                return authenticationResponseDTO;
            }
            throw new InvalidTokenException("Invalid Token!");
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("Invalid token!");
        }
    }

}
