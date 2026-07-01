package com.clinica.universitaria.controller;

import com.clinica.universitaria.dto.DoctorFormDTO;
import com.clinica.universitaria.service.DoctorService;
import com.clinica.universitaria.service.EspecialidadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final EspecialidadService especialidadService;

    @GetMapping("/admin/doctores")
    public String index(@RequestParam(required = false) String q,
                        @RequestParam(required = false) Long especialidadId,
                        @RequestParam(required = false) Boolean estado,
                        Model model) {
        model.addAttribute("pageTitle", "Doctores");
        model.addAttribute("pageDescription", "Gestión del personal médico.");
        model.addAttribute("doctores", doctorService.listar(q, especialidadId, estado));
        model.addAttribute("especialidades", especialidadService.listarActivas());
        model.addAttribute("filtroQ", q);
        model.addAttribute("filtroEspecialidad", especialidadId);
        model.addAttribute("filtroEstado", estado);
        return "admin/doctores/index";
    }

    @GetMapping("/admin/doctores/nuevo")
    public String nuevo(Model model) {
        cargarFormulario(model, new DoctorFormDTO(), "Nuevo doctor", "/admin/doctores/guardar", false);
        return "admin/doctores/form";
    }

    @PostMapping("/admin/doctores/guardar")
    public String guardar(@Valid @ModelAttribute("doctorForm") DoctorFormDTO dto,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            cargarFormulario(model, dto, "Nuevo doctor", "/admin/doctores/guardar", false);
            return "admin/doctores/form";
        }
        try {
            doctorService.crear(dto);
            redirectAttributes.addFlashAttribute("toastMensaje", "Doctor creado correctamente");
            redirectAttributes.addFlashAttribute("toastTipo", "success");
            return "redirect:/admin/doctores";
        } catch (IllegalArgumentException ex) {
            cargarFormulario(model, dto, "Nuevo doctor", "/admin/doctores/guardar", false);
            model.addAttribute("errorGeneral", ex.getMessage());
            return "admin/doctores/form";
        }
    }

    @GetMapping("/admin/doctores/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        var doctor = doctorService.obtenerPorId(id);
        DoctorFormDTO dto = new DoctorFormDTO();
        dto.setDni(doctor.getUsuario().getDni());
        dto.setNombres(doctor.getUsuario().getNombres());
        dto.setApellidos(doctor.getUsuario().getApellidos());
        dto.setCorreo(doctor.getUsuario().getCorreo());
        dto.setTelefono(doctor.getUsuario().getTelefono());
        dto.setEspecialidadId(doctor.getEspecialidad().getId());
        dto.setCmp(doctor.getCmp());
        dto.setExperiencia(doctor.getExperiencia());
        dto.setEstado(doctor.getEstado());
        cargarFormulario(model, dto, "Editar doctor", "/admin/doctores/actualizar/" + id, true);
        return "admin/doctores/form";
    }

    @PostMapping("/admin/doctores/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("doctorForm") DoctorFormDTO dto,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            cargarFormulario(model, dto, "Editar doctor", "/admin/doctores/actualizar/" + id, true);
            return "admin/doctores/form";
        }
        try {
            doctorService.actualizar(id, dto);
            redirectAttributes.addFlashAttribute("toastMensaje", "Doctor actualizado correctamente");
            redirectAttributes.addFlashAttribute("toastTipo", "success");
            return "redirect:/admin/doctores";
        } catch (IllegalArgumentException ex) {
            cargarFormulario(model, dto, "Editar doctor", "/admin/doctores/actualizar/" + id, true);
            model.addAttribute("errorGeneral", ex.getMessage());
            return "admin/doctores/form";
        }
    }

    @PostMapping("/admin/doctores/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        doctorService.desactivar(id);
        redirectAttributes.addFlashAttribute("toastMensaje", "Doctor eliminado correctamente");
        redirectAttributes.addFlashAttribute("toastTipo", "success");
        return "redirect:/admin/doctores";
    }

    @GetMapping("/admin/doctores/ver/{id}")
    public String ver(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Detalle de Doctor");
        model.addAttribute("pageDescription", "Ficha del personal médico.");
        model.addAttribute("doctor", doctorService.obtenerPorId(id));
        return "admin/doctores/detail";
    }

    private void cargarFormulario(Model model, DoctorFormDTO dto, String titulo, String action, boolean modoEdicion) {
        model.addAttribute("pageTitle", titulo);
        model.addAttribute("pageDescription", "Formulario de doctores.");
        model.addAttribute("doctorForm", dto);
        model.addAttribute("especialidades", especialidadService.listarActivas());
        model.addAttribute("formAction", action);
        model.addAttribute("modoEdicion", modoEdicion);
    }
}
