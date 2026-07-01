package com.clinica.universitaria.controller;

import com.clinica.universitaria.dto.EspecialidadFormDTO;
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
public class EspecialidadController {

    private final EspecialidadService especialidadService;

    @GetMapping("/admin/especialidades")
    public String index(Model model) {
        model.addAttribute("pageTitle", "Especialidades");
        model.addAttribute("pageDescription", "Catálogo de especialidades clínicas.");
        model.addAttribute("especialidades", especialidadService.listarTodas());
        return "admin/especialidades/index";
    }

    @GetMapping("/admin/especialidades/nuevo")
    public String nuevo(Model model) {
        cargarFormulario(model, new EspecialidadFormDTO(), "Nueva especialidad", "/admin/especialidades/guardar");
        return "admin/especialidades/form";
    }

    @PostMapping("/admin/especialidades/guardar")
    public String guardar(@Valid @ModelAttribute("especialidadForm") EspecialidadFormDTO dto,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            cargarFormulario(model, dto, "Nueva especialidad", "/admin/especialidades/guardar");
            return "admin/especialidades/form";
        }
        try {
            especialidadService.crear(dto);
            redirectAttributes.addFlashAttribute("toastMensaje", "Especialidad creada correctamente");
            redirectAttributes.addFlashAttribute("toastTipo", "success");
            return "redirect:/admin/especialidades";
        } catch (IllegalArgumentException ex) {
            cargarFormulario(model, dto, "Nueva especialidad", "/admin/especialidades/guardar");
            model.addAttribute("errorGeneral", ex.getMessage());
            return "admin/especialidades/form";
        }
    }

    @GetMapping("/admin/especialidades/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        var especialidad = especialidadService.obtenerPorId(id);
        EspecialidadFormDTO dto = new EspecialidadFormDTO();
        dto.setNombre(especialidad.getNombre());
        dto.setDescripcion(especialidad.getDescripcion());
        dto.setEstado(especialidad.getEstado());
        cargarFormulario(model, dto, "Editar especialidad", "/admin/especialidades/actualizar/" + id);
        return "admin/especialidades/form";
    }

    @PostMapping("/admin/especialidades/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("especialidadForm") EspecialidadFormDTO dto,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            cargarFormulario(model, dto, "Editar especialidad", "/admin/especialidades/actualizar/" + id);
            return "admin/especialidades/form";
        }
        try {
            especialidadService.actualizar(id, dto);
            redirectAttributes.addFlashAttribute("toastMensaje", "Especialidad actualizada correctamente");
            redirectAttributes.addFlashAttribute("toastTipo", "success");
            return "redirect:/admin/especialidades";
        } catch (IllegalArgumentException ex) {
            cargarFormulario(model, dto, "Editar especialidad", "/admin/especialidades/actualizar/" + id);
            model.addAttribute("errorGeneral", ex.getMessage());
            return "admin/especialidades/form";
        }
    }

    @PostMapping("/admin/especialidades/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        especialidadService.desactivar(id);
        redirectAttributes.addFlashAttribute("toastMensaje", "Especialidad eliminada correctamente");
        redirectAttributes.addFlashAttribute("toastTipo", "success");
        return "redirect:/admin/especialidades";
    }

    private void cargarFormulario(Model model, EspecialidadFormDTO dto, String titulo, String action) {
        model.addAttribute("pageTitle", titulo);
        model.addAttribute("pageDescription", "Formulario de especialidades.");
        model.addAttribute("especialidadForm", dto);
        model.addAttribute("formAction", action);
    }
}
