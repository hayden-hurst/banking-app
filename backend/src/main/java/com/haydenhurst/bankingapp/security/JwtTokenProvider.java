package com.haydenhurst.bankingapp.security;

import com.haydenhurst.bankingapp.auth.exception.JwtGenerationException;
import com.haydenhurst.bankingapp.user.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.haydenhurst.bankingapp.security.config.JwtConfig;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// responsible for creating, renewing, and validating JWT tokens
@Component
public class JwtTokenProvider {
    private final JwtConfig jwtConfig;

    public JwtTokenProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    public String generateToken(Authentication authRequest) {
        try{
            UserDetails userDetails = (UserDetails) authRequest.getPrincipal();
            List<String> roles = userDetails.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            // currently direct casting
            // however, if using another login method that returns a different UserDetails implementation we would need a safe check
            User user = (User) userDetails;


            // create token with Jwts builder and return it
            return Jwts.builder()
                    .subject(user.getId().toString())
                    .claim("roles", roles)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + jwtConfig.getJwtExpiration())) // expiration from application.properties
                    .signWith(jwtConfig.getSecretKey()) // secretKey from application.properties
                    .compact();

        } catch (JwtException ex) {
            Logger.getLogger(JwtTokenProvider.class.getName()).log(Level.SEVERE, null, ex);
            throw new JwtGenerationException();
        }
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) jwtConfig.getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try{
            Jwts.parser()
                    .verifyWith((SecretKey) jwtConfig.getSecretKey())
                    .build()
                    .parseSignedClaims(token); // throws if invalid or expired
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            Logger.getLogger(JwtTokenProvider.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        String subject = extractClaims(token).getSubject();
        try{
            return Long.parseLong(subject);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid user ID in token subject");
        }
    }

    public Date getExpirationDateFromToken(String token) {
        return extractClaims(token).getExpiration();
    }
}
