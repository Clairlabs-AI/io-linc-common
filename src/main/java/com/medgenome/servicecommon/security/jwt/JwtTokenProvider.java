package com.medgenome.servicecommon.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Provider class for JWT token operations.
 * Handles token creation, validation, and parsing.
 */
public class JwtTokenProvider {
    private final JwtProperties jwtProperties;
    private final Key key;

    /**
     * Creates a new JwtTokenProvider.
     *
     * @param jwtProperties JWT configuration properties
     */
    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes());
    }

    /**
     * Creates a JWT token for the given authentication.
     *
     * @param authentication the authentication object
     * @return the JWT token
     */
    public String createToken(Authentication authentication) {
        String username = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Claims claims = Jwts.claims().setSubject(username);
        
        if (!authorities.isEmpty()) {
            claims.put("auth", authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(",")));
        }

        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtProperties.getExpirationMs());

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(key)
            .compact();
    }

    /**
     * Validates a JWT token.
     *
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extracts the authentication from a JWT token.
     *
     * @param token the JWT token
     * @return the Authentication object
     */
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();

        String username = claims.getSubject();
        Collection<? extends GrantedAuthority> authorities = getAuthorities(claims);

        UserDetails principal = new User(username, "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * Gets the username from a JWT token.
     *
     * @param token the JWT token
     * @return the username
     */
    public String getUsername(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }

    /**
     * Gets the user ID from a JWT token.
     *
     * @param token the JWT token
     * @return the user ID
     */
    public String getUserId(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .get("userId", String.class);
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Claims claims) {
        String authString = claims.get("auth", String.class);
        if (authString == null || authString.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        return Arrays.stream(authString.split(","))
            .map(SimpleGrantedAuthority::new)
            .toList();
    }
}