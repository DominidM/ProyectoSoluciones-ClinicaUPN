package com.clinica.universitaria.controller;

import com.clinica.universitaria.service.DoctorService;
import com.clinica.universitaria.service.PacienteService;
import com.clinica.universitaria.service.PracticanteService;
import com.clinica.universitaria.service.UsuarioService;
import com.clinica.universitaria.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PerfilController {

    private final UsuarioService usuarioService;
    private final DoctorService doctorService;
    private final PracticanteService practicanteService;
    private final PacienteService pacienteService;

    @GetMapping("/admin/perfil")
    public String perfilAdmin(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("pageTitle", "Mi Perfil");
        model.addAttribute("pageDescription", "Perfil de usuario de solo lectura.");
        model.addAttribute("usuario", usuarioService.obtenerPorCorreo(userDetails.getUsername()));
        return "admin/perfil";
    }

    @GetMapping("/doctor/perfil")
    public String perfilDoctor(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("pageTitle", "Mi Perfil");
        model.addAttribute("pageDescription", "Perfil profesional de solo lectura.");
        model.addAttribute("doctor", doctorService.obtenerPorCorreo(userDetails.getUsername()));
        return "doctor/perfil";
    }

    @GetMapping("/practicante/perfil")
    public String perfilPracticante(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("pageTitle", "Mi Perfil");
        model.addAttribute("pageDescription", "Perfil académico de solo lectura.");
        model.addAttribute("practicante", practicanteService.obtenerPorCorreo(userDetails.getUsername()));
        return "practicante/perfil";
    }

    @GetMapping("/paciente/perfil")
    public String perfilPaciente(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("pageTitle", "Mi Perfil");
        model.addAttribute("pageDescription", "Perfil personal de solo lectura.");
        model.addAttribute("paciente", pacienteService.obtenerPorCorreo(userDetails.getUsername()));
        return "paciente/perfil";
    }
}
