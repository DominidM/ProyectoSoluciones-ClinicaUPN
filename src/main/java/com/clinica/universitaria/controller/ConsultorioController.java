package com.clinica.universitaria.controller;

import com.clinica.universitaria.dto.ConsultorioFormDTO;
import com.clinica.universitaria.service.ConsultorioService;
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
public class ConsultorioController {

    private final ConsultorioService consultorioService;
    private final EspecialidadService especialidadService;

    @GetMapping("/admin/consultorios")
    public String index(@RequestParam(required = false) String q,
                        @RequestParam(required = false) Long especialidadId,
                        @RequestParam(required = false) Boolean estado,
                        Model model) {
        model.addAttribute("pageTitle", "Consultorios");
        model.addAttribute("pageDescription", "Gestión de consultorios y ambientes clínicos.");
        model.addAttribute("consultorios", consultorioService.listar(q, especialidadId, estado));
        model.addAttribute("especialidades", especialidadService.listarActivas());
        model.addAttribute("filtroQ", q);
        model.addAttribute("filtroEspecialidad", especialidadId);
        model.addAttribute("filtroEstado", estado);
        return "admin/consultorios/index";
    }

    @GetMapping("/admin/consultorios/nuevo")
    public String nuevo(Model model) {
        cargarFormulario(model, new ConsultorioFormDTO(), "Nuevo consultorio", "/admin/consultorios/guardar");
        return "admin/consultorios/form";
    }

    @PostMapping("/admin/consultorios/guardar")
    public String guardar(@Valid @ModelAttribute("consultorioForm") ConsultorioFormDTO dto,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            cargarFormulario(model, dto, "Nuevo consultorio", "/admin/consultorios/guardar");
            return "admin/consultorios/form";
        }
        try {
            consultorioService.crear(dto);
            redirectAttributes.addFlashAttribute("toastMensaje", "Consultorio creado correctamente");
            redirectAttributes.addFlashAttribute("toastTipo", "success");
            return "redirect:/admin/consultorios";
        } catch (IllegalArgumentException ex) {
            cargarFormulario(model, dto, "Nuevo consultorio", "/admin/consultorios/guardar");
            model.addAttribute("errorGeneral", ex.getMessage());
            return "admin/consultorios/form";
        }
    }

    @GetMapping("/admin/consultorios/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        var consultorio = consultorioService.obtenerPorId(id);
        ConsultorioFormDTO dto = new ConsultorioFormDTO();
        dto.setEspecialidadId(consultorio.getEspecialidad().getId());
        dto.setNombre(consultorio.getNombre());
        dto.setNumero(consultorio.getNumero());
        dto.setPiso(consultorio.getPiso());
        dto.setDescripcion(consultorio.getDescripcion());
        dto.setEstado(consultorio.getEstado());
        cargarFormulario(model, dto, "Editar consultorio", "/admin/consultorios/actualizar/" + id);
        return "admin/consultorios/form";
    }

    @PostMapping("/admin/consultorios/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("consultorioForm") ConsultorioFormDTO dto,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            cargarFormulario(model, dto, "Editar consultorio", "/admin/consultorios/actualizar/" + id);
            return "admin/consultorios/form";
        }
        try {
            consultorioService.actualizar(id, dto);
            redirectAttributes.addFlashAttribute("toastMensaje", "Consultorio actualizado correctamente");
            redirectAttributes.addFlashAttribute("toastTipo", "success");
            return "redirect:/admin/consultorios";
        } catch (IllegalArgumentException ex) {
            cargarFormulario(model, dto, "Editar consultorio", "/admin/consultorios/actualizar/" + id);
            model.addAttribute("errorGeneral", ex.getMessage());
            return "admin/consultorios/form";
        }
    }

    @PostMapping("/admin/consultorios/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        consultorioService.desactivar(id);
        redirectAttributes.addFlashAttribute("toastMensaje", "Consultorio eliminado correctamente");
        redirectAttributes.addFlashAttribute("toastTipo", "success");
        return "redirect:/admin/consultorios";
    }

    private void cargarFormulario(Model model, ConsultorioFormDTO dto, String titulo, String action) {
        model.addAttribute("pageTitle", titulo);
        model.addAttribute("pageDescription", "Formulario de consultorios.");
        model.addAttribute("consultorioForm", dto);
        model.addAttribute("especialidades", especialidadService.listarActivas());
        model.addAttribute("formAction", action);
    }
}
