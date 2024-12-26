package com.project.bookseller.service;


import com.project.bookseller.authentication.UserDetails;
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

//setSigningKey() method expects a byte[] ??? type when we use SECRET_KEY as a String ???
@Service
@RequiredArgsConstructor
public class TokenService {
    private static final String SECRET_KEY = "eyJ0eXwewesdsuwIsSDSDJKewwjsdWIKAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9";
    private static final String ISSUER_ID = "b2f7c240-8963-4f67-a86c-7e0dbb6f217d";
    //use ENV VARIABLES instead
    private static final int accessTokenExpiredAfter = 1000 * 60 * 15; //5 MINUTES
    private static final int refreshTokenExpiredAfter = 1000 * 60 * 2880;
    private final UserDetailsService userDetailsService;

    public String generateAccessToken(UserDetails userDetails, String sessionId) {
        return Jwts.builder().setSubject(userDetails.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiredAfter))
                .setIssuer(ISSUER_ID)
                .setAudience(sessionId)
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY)), SignatureAlgorithm.HS256).compact();
    }

    public String generateRefreshToken(UserDetails userDetails, String sessionId) {
        Map<String, String> claims = new HashMap<>();
        claims.put("sessionId", sessionId);
        return Jwts.builder().setSubject(userDetails.getEmail())
                .setIssuedAt(new Date())
                .setClaims(claims)
                .setAudience(sessionId)
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiredAfter))
                .setIssuer(ISSUER_ID)
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY)), SignatureAlgorithm.HS256).compact();
    }

    public Claims extractClaims(String token) throws InvalidTokenException {
        try {
            return Jwts.parserBuilder().setSigningKey(TokenService.SECRET_KEY).build().parseClaimsJws(token).getBody();
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid Token!");
        }
    }

    public UserDetails validateAccessToken(String accessToken) throws ExpiredJwtException, InvalidTokenException {
        try {
            Claims claims = extractClaims(accessToken);
            String identifier = claims.getSubject();
            String issuer = claims.getIssuer();
            UserDetails userDetails = userDetailsService.loadUserDetailsByIdentifier(identifier);
            if (userDetails != null && issuer.equals(ISSUER_ID)) {
                return userDetails;
            }
        } catch (Exception e) {
            throw new InvalidTokenException(e.getMessage());
        }
        throw new InvalidTokenException("Invalid token!");
    }

    public Map<String, String> validateRefreshToken(String refreshToken, Session session) throws InvalidTokenException, ExpiredJwtException {
        try {
            Claims claims = extractClaims(refreshToken);
            String identifier = claims.getSubject();
            UserDetails userDetails = userDetailsService.loadUserDetailsByIdentifier(identifier);
            Map<String, String> map = new HashMap<>();
            if (userDetails != null && claims.getExpiration().after(new Date())) {
                map.put("accessToken", generateAccessToken(userDetails, session.getSessionId()));
                map.put("refreshToken", generateRefreshToken(userDetails, session.getSessionId()));
                return map;
            }
            return map;
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("Invalid token!");
        }
    }

}
