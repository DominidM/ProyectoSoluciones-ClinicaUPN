package com.clinica.universitaria.controller;

import com.clinica.universitaria.dto.PracticanteFormDTO;
import com.clinica.universitaria.service.DoctorService;
import com.clinica.universitaria.service.EspecialidadService;
import com.clinica.universitaria.service.PracticanteService;
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
public class PracticanteController {

    private final PracticanteService practicanteService;
    private final EspecialidadService especialidadService;
    private final DoctorService doctorService;

    @GetMapping("/admin/practicantes")
    public String index(@RequestParam(required = false) String q,
                        @RequestParam(required = false) Long supervisorId,
                        @RequestParam(required = false) Boolean estado,
                        Model model) {
        model.addAttribute("pageTitle", "Practicantes");
        model.addAttribute("pageDescription", "Gestión académica y clínica de practicantes.");
        model.addAttribute("practicantes", practicanteService.listar(q, supervisorId, estado));
        model.addAttribute("doctores", doctorService.listar(null, null, true));
        model.addAttribute("filtroQ", q);
        model.addAttribute("filtroSupervisor", supervisorId);
        model.addAttribute("filtroEstado", estado);
        return "admin/practicantes/index";
    }

    @GetMapping("/admin/practicantes/nuevo")
    public String nuevo(Model model) {
        cargarFormulario(model, new PracticanteFormDTO(), "Nuevo practicante", "/admin/practicantes/guardar", false);
        return "admin/practicantes/form";
    }

    @PostMapping("/admin/practicantes/guardar")
    public String guardar(@Valid @ModelAttribute("practicanteForm") PracticanteFormDTO dto,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            cargarFormulario(model, dto, "Nuevo practicante", "/admin/practicantes/guardar", false);
            return "admin/practicantes/form";
        }
        try {
            practicanteService.crear(dto);
            redirectAttributes.addFlashAttribute("toastMensaje", "Practicante creado correctamente");
            redirectAttributes.addFlashAttribute("toastTipo", "success");
            return "redirect:/admin/practicantes";
        } catch (IllegalArgumentException ex) {
            cargarFormulario(model, dto, "Nuevo practicante", "/admin/practicantes/guardar", false);
            model.addAttribute("errorGeneral", ex.getMessage());
            return "admin/practicantes/form";
        }
    }

    @GetMapping("/admin/practicantes/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        var practicante = practicanteService.obtenerPorId(id);
        PracticanteFormDTO dto = new PracticanteFormDTO();
        dto.setDni(practicante.getUsuario().getDni());
        dto.setNombres(practicante.getUsuario().getNombres());
        dto.setApellidos(practicante.getUsuario().getApellidos());
        dto.setCorreo(practicante.getUsuario().getCorreo());
        dto.setTelefono(practicante.getUsuario().getTelefono());
        dto.setEspecialidadId(practicante.getEspecialidad().getId());
        dto.setDoctorSupervisorId(practicante.getDoctorSupervisor() == null ? null : practicante.getDoctorSupervisor().getId());
        dto.setCodigoUniversitario(practicante.getCodigoUniversitario());
        dto.setCiclo(practicante.getCiclo());
        dto.setEstado(practicante.getEstado());
        cargarFormulario(model, dto, "Editar practicante", "/admin/practicantes/actualizar/" + id, true);
        return "admin/practicantes/form";
    }

    @PostMapping("/admin/practicantes/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("practicanteForm") PracticanteFormDTO dto,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            cargarFormulario(model, dto, "Editar practicante", "/admin/practicantes/actualizar/" + id, true);
            return "admin/practicantes/form";
        }
        try {
            practicanteService.actualizar(id, dto);
            redirectAttributes.addFlashAttribute("toastMensaje", "Practicante actualizado correctamente");
            redirectAttributes.addFlashAttribute("toastTipo", "success");
            return "redirect:/admin/practicantes";
        } catch (IllegalArgumentException ex) {
            cargarFormulario(model, dto, "Editar practicante", "/admin/practicantes/actualizar/" + id, true);
            model.addAttribute("errorGeneral", ex.getMessage());
            return "admin/practicantes/form";
        }
    }

    @PostMapping("/admin/practicantes/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        practicanteService.desactivar(id);
        redirectAttributes.addFlashAttribute("toastMensaje", "Practicante eliminado correctamente");
        redirectAttributes.addFlashAttribute("toastTipo", "success");
        return "redirect:/admin/practicantes";
    }

    @GetMapping("/admin/practicantes/ver/{id}")
    public String ver(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Detalle de Practicante");
        model.addAttribute("pageDescription", "Ficha académica del practicante.");
        model.addAttribute("practicante", practicanteService.obtenerPorId(id));
        return "admin/practicantes/detail";
    }

    @GetMapping("/doctor/practicantes")
    public String doctorPracticantes(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        var doctor = doctorService.obtenerPorCorreo(userDetails.getUsername());
        model.addAttribute("pageTitle", "Practicantes Asignados");
        model.addAttribute("pageDescription", "Practicantes supervisados actualmente.");
        model.addAttribute("practicantes", practicanteService.listar(null, doctor.getId(), true));
        return "doctor/practicantes-asignados";
    }

    @GetMapping("/practicante/mi-supervisor")
    public String miSupervisor(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        var practicante = practicanteService.obtenerPorCorreo(userDetails.getUsername());
        model.addAttribute("pageTitle", "Mi Supervisor");
        model.addAttribute("pageDescription", "Información del doctor supervisor asignado.");
        model.addAttribute("practicante", practicante);
        model.addAttribute("supervisor", practicante.getDoctorSupervisor());
        return "practicante/mi-supervisor";
    }

    private void cargarFormulario(Model model, PracticanteFormDTO dto, String titulo, String action, boolean modoEdicion) {
        model.addAttribute("pageTitle", titulo);
        model.addAttribute("pageDescription", "Formulario de practicantes.");
        model.addAttribute("practicanteForm", dto);
        model.addAttribute("especialidades", especialidadService.listarActivas());
        model.addAttribute("doctores", doctorService.listar(null, null, true));
        model.addAttribute("formAction", action);
        model.addAttribute("modoEdicion", modoEdicion);
    }
}
