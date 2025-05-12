package com.medgenome.servicecommon.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SecurityLogoutSuccessHandler implements LogoutSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger("com.medgenome.security");

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {

        String username = (authentication != null && authentication.getName() != null)
                ? authentication.getName()
                : null;

        MDC.put("user", username);
        logger.info("Logout successful for user '{}'", username);
        MDC.clear();

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().flush();
    }
}

