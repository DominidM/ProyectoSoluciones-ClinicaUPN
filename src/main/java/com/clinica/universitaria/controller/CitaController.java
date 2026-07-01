package com.clinica.universitaria.controller;

import com.clinica.universitaria.dto.CitaFormDTO;
import com.clinica.universitaria.model.EstadoCita;
import com.clinica.universitaria.service.*;
import com.clinica.universitaria.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalTime;

@Controller
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;
    private final PacienteService pacienteService;
    private final DoctorService doctorService;
    private final EspecialidadService especialidadService;
    private final ConsultorioService consultorioService;

    @GetMapping("/admin/citas")
    public String index(@RequestParam(required = false) String q,
                        @RequestParam(required = false) EstadoCita estado,
                        Model model) {
        model.addAttribute("pageTitle", "Citas");
        model.addAttribute("pageDescription", "Gestiona las citas médicas presenciales.");
        model.addAttribute("citas", citaService.listarAdmin(q, estado));
        model.addAttribute("estados", EstadoCita.values());
        model.addAttribute("filtroQ", q);
        model.addAttribute("filtroEstado", estado);
        return "admin/citas/index";
    }

    @GetMapping("/admin/citas/nueva")
    public String nueva(Model model) {
        CitaFormDTO dto = new CitaFormDTO();
        dto.setHoraInicio(LocalTime.of(9, 0));
        dto.setHoraFin(LocalTime.of(9, 30));
        cargarFormularioAdmin(model, dto, "Nueva cita", "/admin/citas/guardar");
        return "admin/citas/form";
    }

    @PostMapping("/admin/citas/guardar")
    public String guardar(@Valid @ModelAttribute("citaForm") CitaFormDTO dto,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            cargarFormularioAdmin(model, dto, "Nueva cita", "/admin/citas/guardar");
            return "admin/citas/form";
        }
        try {
            citaService.crearComoAdmin(dto);
            redirectAttributes.addFlashAttribute("toastMensaje", "Cita creada correctamente");
            redirectAttributes.addFlashAttribute("toastTipo", "success");
            return "redirect:/admin/citas";
        } catch (IllegalArgumentException ex) {
            cargarFormularioAdmin(model, dto, "Nueva cita", "/admin/citas/guardar");
            model.addAttribute("errorGeneral", ex.getMessage());
            return "admin/citas/form";
        }
    }

    @PostMapping({"/admin/citas/eliminar/{id}", "/admin/citas/cancelar/{id}"})
    public String cancelarAdmin(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        citaService.cancelar(id);
        redirectAttributes.addFlashAttribute("toastMensaje", "Cita eliminada correctamente");
        redirectAttributes.addFlashAttribute("toastTipo", "success");
        return "redirect:/admin/citas";
    }

    @GetMapping("/admin/citas/ver/{id}")
    public String ver(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Detalle de Cita");
        model.addAttribute("pageDescription", "Vista completa de la cita.");
        model.addAttribute("cita", citaService.obtenerPorId(id));
        return "admin/citas/detail";
    }

    @GetMapping("/doctor/mis-citas")
    public String misCitasDoctor(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        var doctor = doctorService.obtenerPorCorreo(userDetails.getUsername());
        model.addAttribute("pageTitle", "Mis Citas");
        model.addAttribute("pageDescription", "Agenda clínica del doctor.");
        model.addAttribute("citas", citaService.listarPorDoctor(doctor));
        return "doctor/mis-citas";
    }

    @GetMapping("/paciente/mis-citas")
    public String misCitasPaciente(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        var paciente = pacienteService.obtenerPorCorreo(userDetails.getUsername());
        model.addAttribute("pageTitle", "Mis Citas");
        model.addAttribute("pageDescription", "Historial de tus citas médicas.");
        model.addAttribute("citas", citaService.listarPorPaciente(paciente));
        return "paciente/mis-citas";
    }

    @GetMapping("/paciente/solicitar-cita")
    public String solicitarCita(Model model) {
        CitaFormDTO dto = new CitaFormDTO();
        dto.setHoraInicio(LocalTime.of(9, 0));
        dto.setHoraFin(LocalTime.of(9, 30));
        cargarFormularioPaciente(model, dto);
        return "paciente/solicitar-cita";
    }

    @PostMapping("/paciente/solicitar-cita")
    public String guardarSolicitud(@AuthenticationPrincipal CustomUserDetails userDetails,
                                   @Valid @ModelAttribute("citaForm") CitaFormDTO dto,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        var paciente = pacienteService.obtenerPorCorreo(userDetails.getUsername());
        if (dto.getHoraFin() == null && dto.getHoraInicio() != null) {
            dto.setHoraFin(dto.getHoraInicio().plusMinutes(30));
        }
        if (bindingResult.hasErrors()) {
            cargarFormularioPaciente(model, dto);
            return "paciente/solicitar-cita";
        }
        try {
            citaService.solicitarComoPaciente(paciente, dto);
            redirectAttributes.addFlashAttribute("toastMensaje", "Cita solicitada correctamente");
            redirectAttributes.addFlashAttribute("toastTipo", "success");
            return "redirect:/paciente/mis-citas";
        } catch (IllegalArgumentException ex) {
            cargarFormularioPaciente(model, dto);
            model.addAttribute("errorGeneral", ex.getMessage());
            return "paciente/solicitar-cita";
        }
    }

    private void cargarFormularioAdmin(Model model, CitaFormDTO dto, String titulo, String action) {
        model.addAttribute("pageTitle", titulo);
        model.addAttribute("pageDescription", "Formulario de citas médicas.");
        model.addAttribute("citaForm", dto);
        model.addAttribute("pacientes", pacienteService.listar(null, true));
        model.addAttribute("doctores", doctorService.listar(null, null, true));
        model.addAttribute("especialidades", especialidadService.listarActivas());
        model.addAttribute("consultorios", consultorioService.listar(null, null, true));
        model.addAttribute("estados", EstadoCita.values());
        model.addAttribute("formAction", action);
    }

    private void cargarFormularioPaciente(Model model, CitaFormDTO dto) {
        if (dto.getHoraFin() == null && dto.getHoraInicio() != null) {
            dto.setHoraFin(dto.getHoraInicio().plusMinutes(30));
        }
        model.addAttribute("pageTitle", "Solicitar Cita");
        model.addAttribute("pageDescription", "Reserva una cita presencial para ti.");
        model.addAttribute("citaForm", dto);
        model.addAttribute("especialidades", especialidadService.listarActivas());
        model.addAttribute("doctores", doctorService.listar(null, null, true));
    }
}
