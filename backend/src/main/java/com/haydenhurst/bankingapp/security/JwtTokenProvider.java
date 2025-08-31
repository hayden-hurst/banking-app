package com.haydenhurst.bankingapp.security;

import com.haydenhurst.bankingapp.auth.exception.JwtGenerationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.haydenhurst.bankingapp.config.JwtConfig;

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

            // create token with Jwts builder and return it
            return Jwts.builder()
                    .subject(userDetails.getUsername()) // email
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

    public String getUsernameFromToken(String token) {
        return extractClaims(token).getSubject();
    }

    public Date getExpirationDateFromToken(String token) {
        return extractClaims(token).getExpiration();
    }

    public List<String> getRolesFromToken(String token) {
        //
        List<?> rawList = (List<?>) extractClaims(token).get("roles", List.class);
        return rawList.stream()
                .map(Object::toString) // make sure all elements are a string
                .toList();
    }
}
