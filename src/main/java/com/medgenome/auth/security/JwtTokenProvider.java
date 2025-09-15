package com.medgenome.auth.security;

import com.medgenome.auth.config.MultiTenantAuthProperties;
import com.medgenome.auth.dto.TokenDetails;
import com.medgenome.auth.entity.Application;
import com.medgenome.auth.entity.Domain;
import com.medgenome.auth.entity.Role;
import com.medgenome.auth.entity.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {


    @Value("${jwt.private-key.path}")
    private Resource privateKeyPath;

    @Value("${jwt.public-key.path}")
    private Resource publicKeyPath;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    private final MultiTenantAuthProperties properties;

    public JwtTokenProvider(MultiTenantAuthProperties properties) {
        this.properties = properties;
    }


    @PostConstruct
    public void initKeys() throws Exception {
        this.privateKey = loadPrivateKey(privateKeyPath);
        this.publicKey = loadPublicKey(publicKeyPath);
    }

    private PrivateKey loadPrivateKey(Resource resource) throws Exception {
        try (InputStream is = resource.getInputStream()) {
            byte[] keyBytes = is.readAllBytes();
            String key = new String(keyBytes)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        }
    }

    private PublicKey loadPublicKey(Resource resource) throws Exception {
        try (InputStream is = resource.getInputStream()) {
            byte[] keyBytes = is.readAllBytes();
            String key = new String(keyBytes)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(key);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            return KeyFactory.getInstance("RSA").generatePublic(keySpec);
        }
    }

    public String generateToken(String username, Integer tenantId, List<String> roles,
                                List<String> apps, List<String> domains) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + properties.getJwt().getAccessTokenValidityMinutes() * 60 * 1000);

        return Jwts.builder()
                .setSubject(username) // sub (user_id)
                .claim("tenant_id", tenantId) // tenant_id
                // .claim("roles", roles) // roles
                // .claim("apps", apps) // apps
                //.claim("domains", domains) // domains
                .setIssuedAt(now)
                .claim("ist", now.getTime()) // Set issued timestamp
                .claim("ttl", properties.getJwt().getAccessTokenValidityMinutes() * 60 * 1000) // Set time to live in milliseconds
                .setExpiration(expiry) // exp
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public String generateToken(String username, Integer tenantId, List<String> roles,
                           List<String> apps, List<String> domains, long validityMillis) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityMillis);

        return Jwts.builder()
                .setSubject(username)
                .claim("tenant_id", tenantId)
                .claim("roles", roles)
                .claim("apps", apps)
                .claim("domains", domains)
                .setIssuedAt(now)
                .claim("ist", now.getTime())
                .claim("ttl", validityMillis)
                .setExpiration(expiry)
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public TokenDetails parseToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String username = claims.getSubject();
        Integer tenantId = claims.get("tenant_id", Integer.class);
        String encodedPassword = claims.get("password", String.class);
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        @SuppressWarnings("unchecked")
        List<String> allowedApplications = claims.get("apps", List.class);
        @SuppressWarnings("unchecked")
        List<String> domains = claims.get("domains", List.class);
        long issuedTimestamp = claims.get("ist", Long.class);
        long timeToLive = claims.get("ttl", Long.class);

        return new TokenDetails(username, encodedPassword, tenantId, roles, allowedApplications, domains, issuedTimestamp, timeToLive);
    }
}
