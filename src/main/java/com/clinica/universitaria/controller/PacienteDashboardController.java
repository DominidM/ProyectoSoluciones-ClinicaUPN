package com.clinica.universitaria.controller;

import com.clinica.universitaria.model.Paciente;
import com.clinica.universitaria.service.AtencionClinicaService;
import com.clinica.universitaria.service.CitaService;
import com.clinica.universitaria.service.EspecialidadService;
import com.clinica.universitaria.service.PacienteService;
import com.clinica.universitaria.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PacienteDashboardController {

    private final PacienteService pacienteService;
    private final CitaService citaService;
    private final EspecialidadService especialidadService;
    private final AtencionClinicaService atencionClinicaService;

    @GetMapping("/paciente/inicio")
    public String inicio(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Paciente paciente = pacienteService.obtenerPorCorreo(userDetails.getUsername());
        var citas = citaService.listarPorPaciente(paciente);
        var proximas = citas.stream()
                .filter(c -> !c.getFechaCita().isBefore(java.time.LocalDate.now()))
                .sorted((a, b) -> {
                    int byDate = a.getFechaCita().compareTo(b.getFechaCita());
                    return byDate != 0 ? byDate : a.getHoraInicio().compareTo(b.getHoraInicio());
                })
                .findFirst().orElse(null);
        model.addAttribute("pageTitle", "Inicio");
        model.addAttribute("pageDescription", "Portal personal del paciente.");
        model.addAttribute("paciente", paciente);
        model.addAttribute("proximaCita", proximas);
        model.addAttribute("especialidades", especialidadService.listarActivas());
        model.addAttribute("metricas", java.util.Map.of(
                "citas", (long) citas.size(),
                "atendidas", citas.stream().filter(c -> c.getEstado() == com.clinica.universitaria.model.EstadoCita.ATENDIDA).count(),
                "atenciones", (long) atencionClinicaService.listarTodas().stream()
                        .filter(a -> a.getHistoriaClinica().getPaciente().getId().equals(paciente.getId())).count()
        ));
        return "paciente/inicio";
    }

    @GetMapping("/paciente/especialidades")
    public String especialidades(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("pageTitle", "Especialidades");
        model.addAttribute("pageDescription", "Áreas disponibles para atención presencial.");
        model.addAttribute("paciente", pacienteService.obtenerPorCorreo(userDetails.getUsername()));
        model.addAttribute("especialidades", especialidadService.listarActivas());
        return "paciente/especialidades";
    }
}
