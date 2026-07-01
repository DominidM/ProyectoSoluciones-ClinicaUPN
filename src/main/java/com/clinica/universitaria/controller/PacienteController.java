package com.clinica.universitaria.controller;

import com.clinica.universitaria.dto.PacienteFormDTO;
import com.clinica.universitaria.service.CitaService;
import com.clinica.universitaria.service.DoctorService;
import com.clinica.universitaria.service.HistoriaClinicaService;
import com.clinica.universitaria.service.PacienteService;
import com.clinica.universitaria.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;
    private final CitaService citaService;
    private final HistoriaClinicaService historiaClinicaService;
    private final DoctorService doctorService;

    @GetMapping("/admin/pacientes")
    public String index(@RequestParam(required = false) String q,
                        @RequestParam(required = false) Boolean estado,
                        Model model) {
        model.addAttribute("pageTitle", "Pacientes");
        model.addAttribute("pageDescription", "Administra pacientes y su información clínica.");
        model.addAttribute("pacientes", pacienteService.listar(q, estado));
        model.addAttribute("filtroQ", q);
        model.addAttribute("filtroEstado", estado);
        return "admin/pacientes/index";
    }

    @GetMapping("/admin/pacientes/nuevo")
    public String nuevo(Model model) {
        cargarFormulario(model, new PacienteFormDTO(), "Nuevo paciente", "/admin/pacientes/guardar", false);
        return "admin/pacientes/form";
    }

    @PostMapping("/admin/pacientes/guardar")
    public String guardar(@Valid @ModelAttribute("pacienteForm") PacienteFormDTO dto,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            cargarFormulario(model, dto, "Nuevo paciente", "/admin/pacientes/guardar", false);
            return "admin/pacientes/form";
        }
        try {
            pacienteService.crear(dto);
            redirectAttributes.addFlashAttribute("toastMensaje", "Paciente creado correctamente");
            redirectAttributes.addFlashAttribute("toastTipo", "success");
            return "redirect:/admin/pacientes";
        } catch (IllegalArgumentException ex) {
            cargarFormulario(model, dto, "Nuevo paciente", "/admin/pacientes/guardar", false);
            model.addAttribute("errorGeneral", ex.getMessage());
            return "admin/pacientes/form";
        }
    }

    @GetMapping("/admin/pacientes/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        var paciente = pacienteService.obtenerPorId(id);
        PacienteFormDTO dto = new PacienteFormDTO();
        dto.setDni(paciente.getUsuario().getDni());
        dto.setNombres(paciente.getUsuario().getNombres());
        dto.setApellidos(paciente.getUsuario().getApellidos());
        dto.setCorreo(paciente.getUsuario().getCorreo());
        dto.setTelefono(paciente.getUsuario().getTelefono());
        dto.setFechaNacimiento(paciente.getFechaNacimiento());
        dto.setSexo(paciente.getSexo());
        dto.setDireccion(paciente.getDireccion());
        dto.setContactoEmergencia(paciente.getContactoEmergencia());
        dto.setTelefonoEmergencia(paciente.getTelefonoEmergencia());
        dto.setEstado(paciente.getEstado());
        cargarFormulario(model, dto, "Editar paciente", "/admin/pacientes/actualizar/" + id, true);
        return "admin/pacientes/form";
    }

    @PostMapping("/admin/pacientes/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("pacienteForm") PacienteFormDTO dto,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            cargarFormulario(model, dto, "Editar paciente", "/admin/pacientes/actualizar/" + id, true);
            return "admin/pacientes/form";
        }
        try {
            pacienteService.actualizar(id, dto);
            redirectAttributes.addFlashAttribute("toastMensaje", "Paciente actualizado correctamente");
            redirectAttributes.addFlashAttribute("toastTipo", "success");
            return "redirect:/admin/pacientes";
        } catch (IllegalArgumentException ex) {
            cargarFormulario(model, dto, "Editar paciente", "/admin/pacientes/actualizar/" + id, true);
            model.addAttribute("errorGeneral", ex.getMessage());
            return "admin/pacientes/form";
        }
    }

    @PostMapping("/admin/pacientes/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        pacienteService.desactivar(id);
        redirectAttributes.addFlashAttribute("toastMensaje", "Paciente eliminado correctamente");
        redirectAttributes.addFlashAttribute("toastTipo", "success");
        return "redirect:/admin/pacientes";
    }

    @GetMapping("/admin/pacientes/ver/{id}")
    public String ver(@PathVariable Long id, Model model) {
        var paciente = pacienteService.obtenerPorId(id);
        model.addAttribute("pageTitle", "Detalle de Paciente");
        model.addAttribute("pageDescription", "Información consolidada del paciente.");
        model.addAttribute("paciente", paciente);
        model.addAttribute("citas", citaService.listarPorPaciente(paciente));
        model.addAttribute("historia", historiaClinicaService.obtenerPorPaciente(paciente));
        return "admin/pacientes/detail";
    }

    @GetMapping("/doctor/mis-pacientes")
    public String misPacientes(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        var doctor = doctorService.obtenerPorCorreo(userDetails.getUsername());
        var pacientes = pacienteService.obtenerPacientesUnicosPorCitas(citaService.listarPorDoctor(doctor));
        model.addAttribute("pageTitle", "Mis Pacientes");
        model.addAttribute("pageDescription", "Pacientes que tuvieron citas contigo.");
        model.addAttribute("pacientes", pacientes);
        return "doctor/mis-pacientes";
    }

    private void cargarFormulario(Model model, PacienteFormDTO dto, String titulo, String action, boolean modoEdicion) {
        model.addAttribute("pageTitle", titulo);
        model.addAttribute("pageDescription", "Formulario de gestión de pacientes.");
        model.addAttribute("pacienteForm", dto);
        model.addAttribute("formAction", action);
        model.addAttribute("modoEdicion", modoEdicion);
        model.addAttribute("submitLabel", modoEdicion ? "Editar paciente" : "Guardar paciente");
    }
}
