package io.linc.auth.util;

import io.linc.auth.dto.AuthRequest;
import io.linc.auth.dto.TokenDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Utility class for extracting information from Spring Security Authentication.
 * Provides backward compatibility for different authentication detail types.
 */
public class AuthenticationUtils {

    private AuthenticationUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Extracts tenant ID from the current authentication context.
     * Supports TokenDetails (new), AuthRequest (legacy), Integer, and String types.
     *
     * @return tenant ID as String, or null if not found
     */
    public static String getTenantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        return getTenantId(authentication);
    }

    /**
     * Extracts tenant ID from the given authentication object.
     * Supports TokenDetails (new), AuthRequest (legacy), Integer, and String types.
     *
     * @param authentication Spring Security authentication object
     * @return tenant ID as String, or null if not found
     */
    public static String getTenantId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        Object details = authentication.getDetails();

        if (details instanceof TokenDetails tokenDetails) {
            return String.valueOf(tokenDetails.tenantId());
        } else if (details instanceof AuthRequest authRequest) {
            return String.valueOf(authRequest.getTenantId());
        } else if (details instanceof Integer) {
            // Backward compatibility: if details is directly an Integer (tenantId)
            return String.valueOf(details);
        } else if (details instanceof String) {
            // Backward compatibility: if details is directly a String (tenantId)
            return (String) details;
        }

        return null;
    }

    /**
     * Extracts tenant ID as Integer from the current authentication context.
     * Supports TokenDetails (new), AuthRequest (legacy), Integer, and String types.
     *
     * @return tenant ID as Integer, or null if not found or cannot be converted
     */
    public static Integer getTenantIdAsInteger() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        return getTenantIdAsInteger(authentication);
    }

    /**
     * Extracts tenant ID as Integer from the given authentication object.
     * Supports TokenDetails (new), AuthRequest (legacy), Integer, and String types.
     *
     * @param authentication Spring Security authentication object
     * @return tenant ID as Integer, or null if not found or cannot be converted
     */
    public static Integer getTenantIdAsInteger(Authentication authentication) {
        String tenantIdStr = getTenantId(authentication);
        if (tenantIdStr == null || tenantIdStr.isEmpty()) {
            return null;
        }

        try {
            return Integer.parseInt(tenantIdStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Gets the full TokenDetails from authentication if available.
     *
     * @return TokenDetails object, or null if not available
     */
    public static TokenDetails getTokenDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        return getTokenDetails(authentication);
    }

    /**
     * Gets the full TokenDetails from the given authentication object if available.
     * Also checks request attributes as TokenDetails may be stored there for backward compatibility.
     *
     * @param authentication Spring Security authentication object
     * @return TokenDetails object, or null if not available
     */
    public static TokenDetails getTokenDetails(Authentication authentication) {
        // First check request attributes (where TokenDetails is stored for backward compatibility)
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                Object tokenDetails = request.getAttribute("tokenDetails");
                if (tokenDetails instanceof TokenDetails) {
                    return (TokenDetails) tokenDetails;
                }
            }
        } catch (Exception e) {
            // Ignore if not in request context
        }

        // Fallback to authentication details (for legacy support)
        if (authentication != null) {
            Object details = authentication.getDetails();
            if (details instanceof TokenDetails tokenDetails) {
                return tokenDetails;
            }
        }

        return null;
    }

    /**
     * Gets the username from the current authentication context.
     *
     * @return username, or null if not found
     */
    public static String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        return authentication.getName();
    }
}

