package com.clinica.universitaria.controller;

import com.clinica.universitaria.model.Doctor;
import com.clinica.universitaria.service.AtencionClinicaService;
import com.clinica.universitaria.service.CitaService;
import com.clinica.universitaria.service.DoctorService;
import com.clinica.universitaria.service.PacienteService;
import com.clinica.universitaria.service.PracticanteService;
import com.clinica.universitaria.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DoctorDashboardController {

    private final DoctorService doctorService;
    private final CitaService citaService;
    private final PacienteService pacienteService;
    private final AtencionClinicaService atencionClinicaService;
    private final PracticanteService practicanteService;

    @GetMapping("/doctor/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Doctor doctor = doctorService.obtenerPorCorreo(userDetails.getUsername());
        var citas = citaService.listarPorDoctor(doctor);
        var pacientes = pacienteService.obtenerPacientesUnicosPorCitas(citas);
        var atenciones = atencionClinicaService.listarPorDoctor(doctor.getId());
        var practicantes = practicanteService.listar(null, doctor.getId(), true);

        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("pageDescription", "Panel clínico del doctor.");
        model.addAttribute("doctor", doctor);
        model.addAttribute("citasHoy", citaService.citasHoyPorDoctor(doctor));
        model.addAttribute("proximasCitas", citaService.proximasPorDoctor(doctor));
        model.addAttribute("pacientesRecientes", pacientes.stream().limit(6).toList());
        model.addAttribute("atencionesRecientes", atenciones.stream().limit(6).toList());
        model.addAttribute("metricas", java.util.Map.of(
                "citasHoy", (long) citaService.citasHoyPorDoctor(doctor).size(),
                "citasPendientes", citas.stream().filter(c -> c.getEstado() == com.clinica.universitaria.model.EstadoCita.PENDIENTE).count(),
                "pacientes", (long) pacientes.size(),
                "atenciones", (long) atenciones.size(),
                "practicantes", (long) practicantes.size()
        ));
        return "doctor/dashboard";
    }
}
