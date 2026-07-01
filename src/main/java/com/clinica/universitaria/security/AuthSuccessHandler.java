package com.clinica.universitaria.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String redirectUrl = "/login";
        if (authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
            redirectUrl = "/admin/dashboard";
        } else if (authentication.getAuthorities().stream().anyMatch(a -> "ROLE_DOCTOR".equals(a.getAuthority()))) {
            redirectUrl = "/doctor/dashboard";
        } else if (authentication.getAuthorities().stream().anyMatch(a -> "ROLE_PRACTICANTE".equals(a.getAuthority()))) {
            redirectUrl = "/practicante/dashboard";
        } else if (authentication.getAuthorities().stream().anyMatch(a -> "ROLE_PACIENTE".equals(a.getAuthority()))) {
            redirectUrl = "/paciente/inicio";
        }
        response.sendRedirect(request.getContextPath() + redirectUrl);
    }
}
