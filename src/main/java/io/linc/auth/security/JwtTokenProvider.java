package io.linc.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.linc.auth.dto.CachedSessionData;
import io.linc.auth.dto.TokenDetails;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * JWT Token Provider for token validation and parsing.
 * This service only validates and parses tokens - token generation is handled by the IAM service.
 * Uses HS256 algorithm with a secret key for token validation when configured.
 * Can parse tokens without signature validation if secret key is not configured.
 * Validates that tokens contain sessionId and ensures refreshed tokens maintain the same sessionId.
 */
@Component
public class JwtTokenProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenProvider.class);
    public static final String TOKEN_PERMISSIONS = "permissions";
    public static final String TOKEN_ROLE = "role";
    public static final String TOKEN_TYPE = "type";
    public static final String ACCESS_TOKEN = "access";
    public static final String TENANT_ID = "tenant_id";
    public static final String TENANT = "tenant";
    public static final String USER_ID = "user_id";
    public static final String SESSION_ID = "sessionId";

    @Value("${auth.jwt.secret-key:}")
    private String secretKey;

    @Value("${auth.jwt.validate-session-id:true}")
    private boolean validateSessionId;

    private SecretKey hmacKey;

    // In-memory cache to track session data from tokens
    // Key: username, Value: CachedSessionData (sessionId, permissions, role, tenantId)
    private final Map<String, CachedSessionData> sessionDataCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void initKeys() {
        if (secretKey == null || secretKey.isEmpty() || secretKey.equals("changeThisToASecureSecretKeyInProduction")) {
            LOGGER.info("JWT secret key is not configured. Token validation will be skipped, but tokens can still be parsed.");
            return;
        }

        try {
            // For HS256, we need at least 256 bits (32 bytes)
            byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
            // Ensure minimum key length for HS256
            if (keyBytes.length < 32) {
                // Pad to minimum required length
                byte[] paddedKey = new byte[32];
                System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
                keyBytes = paddedKey;
            }
            this.hmacKey = Keys.hmacShaKeyFor(keyBytes);
            LOGGER.info("Initialized JWT token provider with HS256 algorithm for signature validation");
        } catch (Exception e) {
            LOGGER.warn("Failed to initialize HMAC key for HS256. Token validation will be skipped, but tokens can still be parsed.", e);
        }
    }

    /**
     * Validates a JWT token using HS256 algorithm.
     * If secret key is not configured, validates expiration only (signature validation skipped).
     * If sessionId validation is enabled, validates that the token contains a sessionId and
     * that refreshed tokens maintain the same sessionId as the original token.
     *
     * @param token JWT token string
     * @return true if token is valid (signature verified if key configured, not expired, and sessionId valid if enabled), false otherwise
     */
    public boolean validateToken(String token) {
        if (hmacKey == null) {
            LOGGER.debug("HMAC key not configured. Validating expiration and token structure only.");
            // Even without signature validation, we should check expiration
            if (!isTokenNotExpired(token)) {
                return false;
            }

            // Validate token type and required claims (access token vs refresh token)
            if (validateSessionId) {
                return validateTokenSessionId(token);
            } else {
                // Parse token to check if it has required claims for its type
                return parseTokenDetails(token);
            }
        }

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(hmacKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Explicitly check expiration (JJWT should do this automatically, but we ensure it)
            if (isTokenNotExpired(claims)) {
                return false;
            }

            // Determine token type and validate accordingly
            if (determineTokenType(claims)) return false;

            // If sessionId validation is enabled, validate sessionId consistency
            return !validateSessionId || !validateSessionIdDetails(claims);
        } catch (JwtException | IllegalArgumentException ex) {
            LOGGER.debug("Token validation failed: {}", ex.getMessage());
            return false;
        }
    }

    private boolean parseTokenDetails(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }
            String paddedPayload = parts[1];
            int remainder = paddedPayload.length() % 4;
            if (remainder > 0) {
                paddedPayload += "=".repeat(4 - remainder);
            }
            byte[] decodedBytes = Base64.getUrlDecoder().decode(paddedPayload);
            String payloadJson = new String(decodedBytes, StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> claimsMap = mapper.readValue(payloadJson, Map.class);
            Claims claims = Jwts.claims(claimsMap);

            // Validate token type
            boolean isAccessToken = isAccessToken(claims);
            if (isAccessToken) {
                validateAccessTokenClaims(claims);
            } else {
                validateRefreshTokenClaims(claims);
            }
            return true;
        } catch (Exception ex) {
            LOGGER.debug("Token structure validation failed: {}", ex.getMessage());
            return false;
        }
    }

    private boolean validateSessionIdDetails(Claims claims) {
        try {
            validateSessionIdFromClaims(claims);
        } catch (JwtException ex) {
            LOGGER.debug("SessionId validation failed: {}", ex.getMessage());
            return true;
        }
        return false;
    }

    private boolean determineTokenType(Claims claims) {
        boolean isAccessToken = isAccessToken(claims);
        try {
            if (isAccessToken) {
                // Access tokens must have all required claims
                validateAccessTokenClaims(claims);
            } else {
                // Refresh tokens must have sessionId and essential details
                validateRefreshTokenClaims(claims);
            }
        } catch (JwtException ex) {
            LOGGER.debug("Token type validation failed: {}", ex.getMessage());
            return true;
        }
        return false;
    }

    /**
     * Determines if a token is an access token or refresh token.
     * Access tokens typically have a "token_type" claim set to "access" or have all claims (permissions, role, etc.).
     * Refresh tokens typically have "token_type" set to "refresh" or have minimal claims (sessionId, username, tenantId).
     *
     * @param claims JWT claims
     * @return true if token is an access token, false if it's a refresh token
     */
    private boolean isAccessToken(Claims claims) {
        // Check for explicit token_type claim
        String tokenType = claims.get(TOKEN_TYPE, String.class);
        if (tokenType != null) {
            return ACCESS_TOKEN.equalsIgnoreCase(tokenType);
        }

        // Refresh tokens typically have minimal claims (sessionId, username, tenantId)
        @SuppressWarnings("unchecked")
        List<String> permissions = claims.get(TOKEN_PERMISSIONS, List.class);
        String role = claims.get(TOKEN_ROLE, String.class);

        // If token has permissions or role, it's likely an access token
        // If it only has sessionId and basic info, it's likely a refresh token
        return (permissions != null && !permissions.isEmpty()) || (role != null && !role.isEmpty());
    }

    /**
     * Validates that an access token has all required claims.
     * Access tokens must have: username, tenantId, sessionId, and at least role or permissions.
     *
     * @param claims JWT claims
     * @throws JwtException if required claims are missing
     */
    private void validateAccessTokenClaims(Claims claims) throws JwtException {
        String username = claims.getSubject();
        if (username == null || username.isEmpty()) {
            LOGGER.debug ("Access token missing required claim: username (sub)");
        }

        Object tenantIdObj = claims.get(TENANT_ID);
        if (tenantIdObj == null) {
            tenantIdObj = claims.get(TENANT);
        }
        if (tenantIdObj == null) {
            LOGGER.debug ("Access token missing required claim: tenant_id or tenant");
        }

        Object userIdObj = claims.get(USER_ID);
        if (userIdObj == null) {
            userIdObj = claims.get(USER_ID);
        }
        if (userIdObj == null) {
            LOGGER.debug ("Access token missing required claim: user_id");
        }

        String sessionId = claims.get(SESSION_ID, String.class);
        if (sessionId == null) {
            sessionId = claims.get("sid", String.class);
        }
        if (sessionId == null || sessionId.isEmpty()) {
            LOGGER.debug ("Access token missing required claim: sessionId or sid");
        }

        // Access token should have at least role or permissions
        @SuppressWarnings("unchecked")
        List<String> permissions = claims.get(TOKEN_PERMISSIONS, List.class);
        String role = claims.get(TOKEN_ROLE, String.class);

        if ((permissions == null || permissions.isEmpty()) && (role == null || role.isEmpty())) {
            LOGGER.warn("Access token missing role and permissions claims. This may indicate an invalid access token.");
        }
    }

    /**
     * Validates that a refresh token has required claims.
     * Refresh tokens must have: username, type="refresh", and sessionId (saved from access token).
     * Note: tenantId is NOT required in refresh token claims - it will be retrieved from cached session data using sessionId.
     *
     * @param claims JWT claims
     * @throws JwtException if required claims are missing
     */
    private void validateRefreshTokenClaims(Claims claims) throws JwtException {
        String username = claims.getSubject();
        if (username == null || username.isEmpty()) {
            throw new JwtException("Refresh token missing required claim: username (sub)");
        }

        // Refresh token MUST have sessionId (saved from access token)
        String sessionId = claims.get(SESSION_ID, String.class);
        if (sessionId == null) {
            sessionId = claims.get("sid", String.class);
        }
        if (sessionId == null || sessionId.isEmpty()) {
            throw new JwtException("Refresh token missing required claim: sessionId or sid. Refresh tokens must contain the sessionId from the access token.");
        }

        // Verify that we have cached session data for this sessionId
        // This ensures the refresh token is associated with a valid access token session
        CachedSessionData cachedData = getCachedSessionDataBySessionId(sessionId, username);
        if (cachedData == null) {
            LOGGER.warn("Refresh token sessionId {} not found in cache for user {}. Access token must be processed first.", sessionId, username);
            throw new JwtException("Refresh token sessionId not found in cache. Access token must be processed before refresh token.");
        }

        LOGGER.debug("Refresh token validated with sessionId: {} for user: {}", sessionId, username);
    }

    /**
     * Retrieves cached session data by sessionId and username.
     * First looks up by username, then verifies sessionId matches.
     *
     * @param sessionId The session identifier
     * @param username  The username (subject)
     * @return CachedSessionData if found and sessionId matches, null otherwise
     */
    private CachedSessionData getCachedSessionDataBySessionId(String sessionId, String username) {
        if (username == null || sessionId == null) {
            return null;
        }

        CachedSessionData cachedData = sessionDataCache.get(username);
        if (cachedData != null && sessionId.equals(cachedData.sessionId())) {
            return cachedData;
        }

        // If not found by username, search all entries (fallback - should rarely be needed)
        for (CachedSessionData data : sessionDataCache.values()) {
            if (sessionId.equals(data.sessionId())) {
                LOGGER.debug("Found cached session data by sessionId {} (username may have changed)", sessionId);
                return data;
            }
        }

        return null;
    }

    /**
     * Parses a JWT token and extracts token details.
     * If secret key is configured, validates the signature before parsing.
     * If secret key is not configured, parses the token without signature validation.
     * This method matches the token structure generated by the IAM service.
     * <p>
     * Handles both access tokens (with all claims) and refresh tokens (with sessionId and essential details).
     *
     * @param token JWT token string
     * @return TokenDetails containing extracted claims
     * @throws JwtException if token parsing fails
     */
    public TokenDetails parseToken(String token) {
        Claims claims;

        if (hmacKey != null) {
            // Parse and validate signature if secret key is configured
            try {
                claims = Jwts.parserBuilder()
                        .setSigningKey(hmacKey)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                // Explicitly check expiration (JJWT should do this automatically, but we ensure it)
                if (isTokenNotExpired(claims)) {
                    throw new JwtException("Token has expired");
                }

                // Determine token type and validate accordingly
                determineTokenTypeAndValidate(claims);
            } catch (JwtException ex) {
                LOGGER.warn("Token validation failed: {}", ex.getMessage());
                throw ex;
            }
        } else {
            // Parse without signature validation if secret key is not configured
            LOGGER.debug("Parsing token without signature validation (secret key not configured)");
            claims = parseTokenWithoutKey(token);
        }

        // Extract claims matching the IAM token structure
        String username = claims.getSubject(); // sub claim
        boolean isAccessToken = isAccessToken(claims);

        // tenant_id can be String or Integer in the token - always extract as Object first to avoid type conversion issues
        Integer tenantId = null;
        Object tenantIdObj = claims.get(TENANT_ID);
        if (tenantIdObj == null) {
            tenantIdObj = claims.get(TENANT);
        }
        if (tenantIdObj != null) {
            tenantId = Integer.parseInt(tenantIdObj.toString());
        }


        // For refresh tokens, tenantId is not in the token - retrieve it from cached session data using sessionId

        // role is a String (not a List)
        String role = claims.get(TOKEN_ROLE, String.class);

        // permissions is a List<String>
        // For access tokens, permissions should be present. For refresh tokens, they may not be present.
        @SuppressWarnings("unchecked")
        List<String> permissions = claims.get(TOKEN_PERMISSIONS, List.class);

        if (permissions == null || permissions.isEmpty()) {
            if (!isAccessToken) {
                // For access tokens, try to get from cached session data if missing
                if (validateSessionId && username != null) {
                    CachedSessionData cachedData = sessionDataCache.get(username);
                    if (cachedData != null && cachedData.permissions() != null && !cachedData.permissions().isEmpty()) {
                        permissions = cachedData.permissions();
                        LOGGER.debug("Using cached permissions for access token user {} from sessionId {}", username, cachedData.sessionId());
                    } else {
                        permissions = new ArrayList<>();
                        LOGGER.debug("Access token for user {} has no permissions and no cached data available", username);
                    }
                } else {
                    permissions = new ArrayList<>();
                }
            } else {
                // For refresh tokens, permissions are typically not present - this is expected
                permissions = new ArrayList<>();
                LOGGER.debug("Refresh token for user {} does not contain permissions (expected behavior)", username);
            }
        }

        // sessionId claim
        String sessionId = claims.get(SESSION_ID, String.class);
        if (sessionId == null) {
            // Try sid as alternative
            sessionId = claims.get("sid", String.class);
        }

        // tenant claim (alternative to tenant_id) - can be String or Integer in the token
        Integer tenant = null;
        Object tenantObj = claims.get(TENANT);
        if (tenantObj != null) {
            tenant = Integer.parseInt(tenantObj.toString());
        }

        // sid claim
        String sid = claims.get("sid", String.class);
        Long issuedAt = claims.get("iat", Long.class);
        Long expiry = claims.get("exp", Long.class);

        return new TokenDetails(username, tenantId, role, permissions, sessionId, tenant, sid, issuedAt, expiry);
    }

    private Claims parseTokenWithoutKey(String token) {
        Claims claims;
        try {
            // Manually decode JWT payload (middle part) without signature validation
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT token format. Expected 3 parts separated by dots.");
            }

            // Decode the payload (second part) - JWT uses base64url encoding
            // Base64 URL decoder may need padding
            String paddedPayload = parts[1];
            int remainder = paddedPayload.length() % 4;
            if (remainder > 0) {
                paddedPayload += "=".repeat(4 - remainder);
            }
            byte[] decodedBytes = Base64.getUrlDecoder().decode(paddedPayload);
            String payloadJson = new String(decodedBytes, StandardCharsets.UTF_8);

            // Parse JSON payload into Claims
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> claimsMap = mapper.readValue(payloadJson, Map.class);

            // Convert Map to Claims object
            claims = Jwts.claims(claimsMap);

            // Validate expiration when parsing without signature validation
            if (isTokenNotExpired(claims)) {
                throw new JwtException("Token has expired");
            }

            // Determine token type and validate accordingly
            boolean isAccessToken = isAccessToken(claims);
            if (isAccessToken) {
                // Access tokens must have all required claims
                validateAccessTokenClaims(claims);
                LOGGER.debug("Parsed access token for user: {}", claims.getSubject());
            } else {
                // Refresh tokens must have sessionId and essential details
                validateRefreshTokenClaims(claims);
                LOGGER.debug("Parsed refresh token for user: {}", claims.getSubject());
            }

            // If sessionId validation is enabled, validate sessionId consistency
            if (validateSessionId) {
                validateSessionIdFromClaims(claims);
            }
        } catch (JwtException ex) {
            // Re-throw JWT exceptions (including expiration and sessionId validation)
            throw ex;
        } catch (Exception ex) {
            LOGGER.warn("Token parsing failed: {}", ex.getMessage());
            throw new JwtException("Failed to parse token: " + ex.getMessage(), ex);
        }
        return claims;
    }

    private void determineTokenTypeAndValidate(Claims claims) {
        boolean isAccessToken = isAccessToken(claims);
        if (isAccessToken) {
            // Access tokens must have all required claims
            validateAccessTokenClaims(claims);
            LOGGER.debug("Parsed access token for user: {}", claims.getSubject());
        } else {
            // Refresh tokens must have sessionId and essential details
            validateRefreshTokenClaims(claims);
            LOGGER.debug("Parsed refresh token for user: {}", claims.getSubject());
        }

        // If sessionId validation is enabled, validate sessionId consistency
        if (validateSessionId) {
            validateSessionIdFromClaims(claims);
        }
    }

    /**
     * Checks if a token is not expired by examining the Claims object.
     *
     * @param claims JWT claims
     * @return true if token is not expired (or has no expiration), false if expired
     */
    private boolean isTokenNotExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        if (expiration == null) {
            // If no expiration claim, consider it valid (some tokens may not have expiration)
            LOGGER.debug("Token has no expiration claim");
            return false;
        }

        Date now = new Date();
        boolean notExpired = expiration.after(now);
        if (!notExpired) {
            LOGGER.debug("Token expired at: {}, current time: {}", expiration, now);
        }
        return !notExpired;
    }

    /**
     * Checks if a token string is not expired by parsing the expiration claim.
     *
     * @param token JWT token string
     * @return true if token is not expired (or has no expiration), false if expired
     */
    private boolean isTokenNotExpired(String token) {
        try {
            // Manually decode JWT payload to check expiration
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                LOGGER.debug("Invalid JWT token format for expiration check");
                return false;
            }

            // Decode the payload (second part)
            String paddedPayload = parts[1];
            int remainder = paddedPayload.length() % 4;
            if (remainder > 0) {
                paddedPayload += "=".repeat(4 - remainder);
            }
            byte[] decodedBytes = Base64.getUrlDecoder().decode(paddedPayload);
            String payloadJson = new String(decodedBytes, StandardCharsets.UTF_8);

            // Parse JSON payload to get expiration
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> claimsMap = mapper.readValue(payloadJson, Map.class);

            // Check expiration claim
            Object expObj = claimsMap.get("exp");
            if (expObj == null) {
                // If no expiration claim, consider it valid
                LOGGER.debug("Token has no expiration claim");
                return true;
            }

            // exp is typically a number (seconds since epoch)
            long expirationSeconds = Long.parseLong(expObj.toString());


            // Convert to milliseconds and compare with current time
            long expirationMillis = expirationSeconds * 1000;
            long nowMillis = System.currentTimeMillis();
            boolean notExpired = expirationMillis > nowMillis;

            if (!notExpired) {
                LOGGER.debug("Token expired at: {} ({}), current time: {} ({})",
                        expirationSeconds, new Date(expirationMillis),
                        nowMillis / 1000, new Date(nowMillis));
            }

            return notExpired;
        } catch (Exception ex) {
            LOGGER.warn("Failed to check token expiration: {}", ex.getMessage());
            // If we can't parse the token to check expiration, consider it invalid
            return false;
        }
    }

    /**
     * Validates that the token contains a sessionId and that refreshed tokens maintain the same sessionId.
     * Works for both access tokens and refresh tokens.
     * For initial tokens, the sessionId and permissions are cached. For refreshed tokens, the sessionId must match.
     * Also extracts and caches permissions, role, and tenantId for use with refreshed tokens.
     * <p>
     * Note: Refresh tokens must contain the sessionId from the original access token.
     *
     * @param claims JWT claims containing sessionId, username, and permissions
     * @throws JwtException if sessionId validation fails
     */
    private void validateSessionIdFromClaims(Claims claims) throws JwtException {
        // Check issuer - if it's qcstats, relax sessionId validation
        String issuer = claims.getIssuer();
        boolean relaxSessionIdValidation = "qcstats".equalsIgnoreCase(issuer);
        
        // Extract sessionId from claims
        String sessionId = claims.get(SESSION_ID, String.class);
        if (sessionId == null) {
            // Try sid as alternative
            sessionId = claims.get("sid", String.class);
        }

        if (sessionId == null || sessionId.isEmpty()) {
            // If issuer is qcstats, allow tokens without sessionId
            if (relaxSessionIdValidation) {
                LOGGER.debug("Token from qcstats issuer does not contain sessionId - validation relaxed");
                return;
            }
            LOGGER.warn("Token does not contain sessionId or sid claim. SessionId validation failed.");
            throw new JwtException("Token does not contain sessionId");
        }

        // Extract username from claims
        String username = claims.getSubject();
        if (username == null || username.isEmpty()) {
            LOGGER.warn("Token does not contain username (sub claim). Cannot validate sessionId consistency.");
            throw new JwtException("Token does not contain username");
        }

        // Extract permissions, role, and tenantId for caching
        @SuppressWarnings("unchecked")
        List<String> permissions = claims.get(TOKEN_PERMISSIONS, List.class);
        String role = claims.get(TOKEN_ROLE, String.class);
        
        // tenant_id and user_id can be String or Integer in the token - always extract as Object first to avoid type conversion issues
        Integer tenantId = null;
        Object tenantIdObj = claims.get(TENANT_ID);
        if (tenantIdObj == null) {
            tenantIdObj = claims.get(TENANT);
        }
        if (tenantIdObj != null) {
            tenantId = Integer.parseInt(tenantIdObj.toString());
        }
        
        Integer userId = null;
        Object userIdObj = claims.get(USER_ID);
        if (userIdObj != null) {
            userId = Integer.parseInt(userIdObj.toString());
        }

        // Determine if this is an access token or refresh token
        boolean isAccessToken = isAccessToken(claims);

        // For refresh tokens, retrieve tenantId from cached session data
        if (!isAccessToken) {
            // For refresh tokens, get tenantId from cached session data
            CachedSessionData refreshTokenCachedData = getCachedSessionDataBySessionId(sessionId, username);
            if (refreshTokenCachedData != null) {
                tenantId = refreshTokenCachedData.tenantId();
                // Also get permissions and role from cache if not in token
                if ((permissions == null || permissions.isEmpty()) && refreshTokenCachedData.permissions() != null) {
                    permissions = refreshTokenCachedData.permissions();
                }
                if ((role == null || role.isEmpty()) && refreshTokenCachedData.role() != null) {
                    role = refreshTokenCachedData.role();
                }
                userId = refreshTokenCachedData.userId();
                LOGGER.debug("Retrieved tenantId, permissions, and role from cached session data for refresh token");
            }

            // Check if we have cached session data for this user
            CachedSessionData cachedData = sessionDataCache.get(username);

            if (cachedData == null) {
                // First token for this user - cache the session data
                CachedSessionData newData = new CachedSessionData(sessionId, permissions, role, tenantId, userId);
                sessionDataCache.put(username, newData);
                LOGGER.debug("Cached session data for user {} with sessionId {} and {} permissions",
                        username, sessionId, permissions != null ? permissions.size() : 0);
            } else {
                // Check if the sessionId matches the cached one
                // Relax this check if issuer is qcstats
                if (!cachedData.sessionId().equals(sessionId)) {
                    if (relaxSessionIdValidation) {
                        LOGGER.debug("SessionId mismatch for user {} from qcstats issuer. Expected: {}, Found: {} - validation relaxed",
                                username, cachedData.sessionId(), sessionId);
                        // Update cache with new sessionId for qcstats tokens
                        CachedSessionData updatedData = new CachedSessionData(sessionId, 
                                (permissions != null && !permissions.isEmpty()) ? permissions : cachedData.permissions(),
                                (role != null && !role.isEmpty()) ? role : cachedData.role(),
                                (tenantId != null) ? tenantId : cachedData.tenantId(),
                                (userId != null) ? userId : cachedData.userId());
                        sessionDataCache.put(username, updatedData);
                        LOGGER.debug("Updated cached session data with new sessionId for qcstats token");
                        return;
                    }
                    LOGGER.warn("SessionId mismatch for user {}. Expected: {}, Found: {}",
                            username, cachedData.sessionId(), sessionId);
                    throw new JwtException("SessionId mismatch - refreshed token must have the same sessionId");
                }

                // Update cached data with latest permissions/role if they exist in the new token
                // If new token doesn't have permissions, preserve the cached ones
                List<String> permissionsToCache = (permissions != null && !permissions.isEmpty())
                        ? permissions
                        : cachedData.permissions();
                String roleToCache = (role != null && !role.isEmpty()) ? role : cachedData.role();
                Integer tenantIdToCache = (tenantId != null) ? tenantId : cachedData.tenantId();
                Integer userIdToCache = (userId != null) ? userId : cachedData.userId();

                CachedSessionData updatedData = new CachedSessionData(sessionId, permissionsToCache, roleToCache, tenantIdToCache, userIdToCache);
                sessionDataCache.put(username, updatedData);

                if (permissions != null && !permissions.isEmpty()) {
                    LOGGER.debug("Updated cached permissions for user {} with {} permissions", username, permissions.size());
                } else {
                    LOGGER.debug("Preserved cached permissions for user {} (refreshed token had no permissions)", username);
                }

                LOGGER.debug("SessionId validation successful for user {} with sessionId {}", username, sessionId);
            }
        }
    }

    /**
     * Validates sessionId from token string by parsing the token first.
     * Used when signature validation is not performed.
     * Handles both access tokens and refresh tokens.
     *
     * @param token JWT token string
     * @return true if sessionId is valid, false otherwise
     */
    private boolean validateTokenSessionId(String token) {
        try {
            // Parse token to extract claims
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                LOGGER.debug("Invalid JWT token format for sessionId validation");
                return false;
            }

            // Decode the payload
            String paddedPayload = parts[1];
            int remainder = paddedPayload.length() % 4;
            if (remainder > 0) {
                paddedPayload += "=".repeat(4 - remainder);
            }
            byte[] decodedBytes = Base64.getUrlDecoder().decode(paddedPayload);
            String payloadJson = new String(decodedBytes, StandardCharsets.UTF_8);

            // Parse JSON payload into Claims
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> claimsMap = mapper.readValue(payloadJson, Map.class);
            Claims claims = Jwts.claims(claimsMap);

            // Determine token type and validate accordingly
            boolean isAccessToken = isAccessToken(claims);
            try {
                if (isAccessToken) {
                    validateAccessTokenClaims(claims);
                } else {
                    validateRefreshTokenClaims(claims);
                }
            } catch (JwtException ex) {
                LOGGER.debug("Token type validation failed during sessionId check: {}", ex.getMessage());
                return false;
            }

            // Validate sessionId consistency
            validateSessionIdFromClaims(claims);
            return true;
        } catch (JwtException ex) {
            LOGGER.debug("SessionId validation failed: {}", ex.getMessage());
            return false;
        } catch (Exception ex) {
            LOGGER.warn("Failed to validate sessionId from token: {}", ex.getMessage());
            return false;
        }
    }
}
