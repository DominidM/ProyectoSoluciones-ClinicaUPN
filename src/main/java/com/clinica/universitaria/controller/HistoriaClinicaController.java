package com.clinica.universitaria.controller;

import com.clinica.universitaria.service.CitaService;
import com.clinica.universitaria.service.DoctorService;
import com.clinica.universitaria.service.HistoriaClinicaService;
import com.clinica.universitaria.service.PacienteService;
import com.clinica.universitaria.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class HistoriaClinicaController {

    private final HistoriaClinicaService historiaClinicaService;
    private final PacienteService pacienteService;
    private final DoctorService doctorService;
    private final CitaService citaService;

    @GetMapping("/admin/historias/{idPaciente}")
    public String historiaAdmin(@PathVariable Long idPaciente, Model model) {
        var paciente = pacienteService.obtenerPorId(idPaciente);
        model.addAttribute("pageTitle", "Historia Clínica");
        model.addAttribute("pageDescription", "Detalle clínico del paciente.");
        model.addAttribute("paciente", paciente);
        model.addAttribute("historia", historiaClinicaService.obtenerPorPaciente(paciente));
        model.addAttribute("citas", citaService.listarPorPaciente(paciente));
        return "admin/historias/detail";
    }

    @GetMapping("/doctor/historia/{idPaciente}")
    public String historiaDoctor(@PathVariable Long idPaciente,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 Model model) {
        var doctor = doctorService.obtenerPorCorreo(userDetails.getUsername());
        var paciente = pacienteService.obtenerPorId(idPaciente);
        boolean acceso = citaService.listarPorDoctor(doctor).stream()
                .anyMatch(c -> c.getPaciente().getId().equals(idPaciente));
        if (!acceso) {
            throw new IllegalArgumentException("No tienes acceso a la historia clínica solicitada");
        }
        model.addAttribute("pageTitle", "Historia Clínica");
        model.addAttribute("pageDescription", "Vista clínica del paciente atendido.");
        model.addAttribute("paciente", paciente);
        model.addAttribute("historia", historiaClinicaService.obtenerPorPaciente(paciente));
        model.addAttribute("citas", citaService.listarPorPaciente(paciente));
        return "doctor/historia-clinica";
    }

    @GetMapping("/paciente/historial")
    public String historialPaciente(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        var paciente = pacienteService.obtenerPorCorreo(userDetails.getUsername());
        model.addAttribute("pageTitle", "Mi Historial");
        model.addAttribute("pageDescription", "Resumen básico de tu historial clínico.");
        model.addAttribute("paciente", paciente);
        model.addAttribute("historia", historiaClinicaService.obtenerPorPaciente(paciente));
        model.addAttribute("citas", citaService.listarPorPaciente(paciente));
        return "paciente/historial";
    }
}
