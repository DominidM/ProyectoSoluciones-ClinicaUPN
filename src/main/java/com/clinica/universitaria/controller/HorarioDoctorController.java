package com.clinica.universitaria.controller;

import com.clinica.universitaria.dto.HorarioDoctorFormDTO;
import com.clinica.universitaria.model.DiaSemana;
import com.clinica.universitaria.service.DoctorService;
import com.clinica.universitaria.service.HorarioDoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class HorarioDoctorController {

    private final HorarioDoctorService horarioDoctorService;
    private final DoctorService doctorService;

    @GetMapping("/admin/horarios")
    public String index(@RequestParam(required = false) Long doctorId,
                        @RequestParam(required = false) Boolean estado,
                        Model model) {
        model.addAttribute("pageTitle", "Horarios Médicos");
        model.addAttribute("pageDescription", "Gestiona los horarios de atención de los doctores.");
        model.addAttribute("horarios", horarioDoctorService.listar(doctorId, estado));
        model.addAttribute("doctores", doctorService.listar(null, null, true));
        model.addAttribute("filtroDoctor", doctorId);
        model.addAttribute("filtroEstado", estado);
        return "admin/horarios/index";
    }

    @GetMapping("/admin/horarios/nuevo")
    public String nuevo(Model model) {
        cargarFormulario(model, new HorarioDoctorFormDTO(), "Nuevo horario", "/admin/horarios/guardar");
        return "admin/horarios/form";
    }

    @PostMapping("/admin/horarios/guardar")
    public String guardar(@Valid @ModelAttribute("horarioForm") HorarioDoctorFormDTO dto,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            cargarFormulario(model, dto, "Nuevo horario", "/admin/horarios/guardar");
            return "admin/horarios/form";
        }
        try {
            horarioDoctorService.crear(dto);
            redirectAttributes.addFlashAttribute("toastMensaje", "Horario creado correctamente");
            redirectAttributes.addFlashAttribute("toastTipo", "success");
            return "redirect:/admin/horarios";
        } catch (IllegalArgumentException ex) {
            cargarFormulario(model, dto, "Nuevo horario", "/admin/horarios/guardar");
            model.addAttribute("errorGeneral", ex.getMessage());
            return "admin/horarios/form";
        }
    }

    @GetMapping("/admin/horarios/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        var horario = horarioDoctorService.obtenerPorId(id);
        HorarioDoctorFormDTO dto = new HorarioDoctorFormDTO();
        dto.setDoctorId(horario.getDoctor().getId());
        dto.setDiaSemana(horario.getDiaSemana());
        dto.setHoraInicio(horario.getHoraInicio());
        dto.setHoraFin(horario.getHoraFin());
        dto.setEstado(horario.getEstado());
        cargarFormulario(model, dto, "Editar horario", "/admin/horarios/actualizar/" + id);
        return "admin/horarios/form";
    }

    @PostMapping("/admin/horarios/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("horarioForm") HorarioDoctorFormDTO dto,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            cargarFormulario(model, dto, "Editar horario", "/admin/horarios/actualizar/" + id);
            return "admin/horarios/form";
        }
        try {
            horarioDoctorService.actualizar(id, dto);
            redirectAttributes.addFlashAttribute("toastMensaje", "Horario actualizado correctamente");
            redirectAttributes.addFlashAttribute("toastTipo", "success");
            return "redirect:/admin/horarios";
        } catch (IllegalArgumentException ex) {
            cargarFormulario(model, dto, "Editar horario", "/admin/horarios/actualizar/" + id);
            model.addAttribute("errorGeneral", ex.getMessage());
            return "admin/horarios/form";
        }
    }

    @PostMapping("/admin/horarios/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        horarioDoctorService.eliminar(id);
        redirectAttributes.addFlashAttribute("toastMensaje", "Horario eliminado correctamente");
        redirectAttributes.addFlashAttribute("toastTipo", "success");
        return "redirect:/admin/horarios";
    }

    private void cargarFormulario(Model model, HorarioDoctorFormDTO dto, String titulo, String action) {
        model.addAttribute("pageTitle", titulo);
        model.addAttribute("pageDescription", "Formulario de horarios.");
        model.addAttribute("horarioForm", dto);
        model.addAttribute("doctores", doctorService.listar(null, null, true));
        model.addAttribute("diasSemana", DiaSemana.values());
        model.addAttribute("formAction", action);
    }
}
