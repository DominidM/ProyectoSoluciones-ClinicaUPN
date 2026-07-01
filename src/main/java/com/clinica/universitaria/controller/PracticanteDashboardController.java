package com.clinica.universitaria.controller;

import com.clinica.universitaria.model.Practicante;
import com.clinica.universitaria.service.AtencionClinicaService;
import com.clinica.universitaria.service.CitaService;
import com.clinica.universitaria.service.EvaluacionPracticanteService;
import com.clinica.universitaria.service.PracticanteService;
import com.clinica.universitaria.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PracticanteDashboardController {

    private final PracticanteService practicanteService;
    private final AtencionClinicaService atencionClinicaService;
    private final EvaluacionPracticanteService evaluacionPracticanteService;
    private final CitaService citaService;

    @GetMapping("/practicante/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Practicante practicante = practicanteService.obtenerPorCorreo(userDetails.getUsername());
        var atenciones = atencionClinicaService.listarPorPracticante(practicante.getId());
        var evaluaciones = evaluacionPracticanteService.listarPorPracticante(practicante.getId());
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("pageDescription", "Panel académico y clínico del practicante.");
        model.addAttribute("practicante", practicante);
        model.addAttribute("metricas", java.util.Map.of(
                "atenciones", (long) atenciones.size(),
                "evaluaciones", (long) evaluaciones.size(),
                "promedio", evaluacionPracticanteService.promedioPorPracticante(practicante.getId()),
                "supervisor", practicante.getDoctorSupervisor() == null ? 0L : 1L
        ));
        model.addAttribute("ultimasEvaluaciones", evaluaciones.stream().limit(5).toList());
        model.addAttribute("ultimasAtenciones", atenciones.stream().limit(5).toList());
        return "practicante/dashboard";
    }

    @GetMapping("/practicante/mis-asignaciones")
    public String misAsignaciones(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Practicante practicante = practicanteService.obtenerPorCorreo(userDetails.getUsername());
        var asignaciones = practicante.getDoctorSupervisor() == null
                ? java.util.List.of()
                : citaService.listarPorDoctor(practicante.getDoctorSupervisor());
        model.addAttribute("pageTitle", "Mis Asignaciones");
        model.addAttribute("pageDescription", "Citas y actividades asociadas a tu supervisor.");
        model.addAttribute("practicante", practicante);
        model.addAttribute("asignaciones", asignaciones);
        return "practicante/mis-asignaciones";
    }
}
