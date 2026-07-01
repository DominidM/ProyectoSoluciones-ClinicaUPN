package com.clinica.universitaria.controller;

import com.clinica.universitaria.model.Usuario;
import com.clinica.universitaria.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributeAdvice {

    private final UsuarioService usuarioService;

    @ModelAttribute("usuarioActual")
    public Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        try {
            return usuarioService.obtenerPorCorreo(auth.getName());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @ModelAttribute("fechaActual")
    public LocalDate fechaActual() {
        return LocalDate.now();
    }

    @ModelAttribute("rutaActual")
    public String rutaActual(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
