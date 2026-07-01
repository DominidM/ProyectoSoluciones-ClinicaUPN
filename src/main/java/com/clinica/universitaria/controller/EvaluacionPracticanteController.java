package com.clinica.universitaria.controller;

import com.clinica.universitaria.dto.EvaluacionPracticanteFormDTO;
import com.clinica.universitaria.service.DoctorService;
import com.clinica.universitaria.service.EvaluacionPracticanteService;
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

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class EvaluacionPracticanteController {

    private final EvaluacionPracticanteService evaluacionPracticanteService;
    private final PracticanteService practicanteService;
    private final DoctorService doctorService;

    @GetMapping("/admin/evaluaciones")
    public String indexAdmin(Model model) {
        model.addAttribute("pageTitle", "Evaluaciones");
        model.addAttribute("pageDescription", "Gestiona evaluaciones de practicantes.");
        model.addAttribute("evaluaciones", evaluacionPracticanteService.listarTodas());
        return "admin/evaluaciones/index";
    }

    @GetMapping("/admin/evaluaciones/nueva")
    public String nuevaAdmin(Model model) {
        EvaluacionPracticanteFormDTO dto = new EvaluacionPracticanteFormDTO();
        dto.setFechaEvaluacion(LocalDate.now());
        dto.setEstado(true);
        cargarFormulario(model, dto, "Nueva evaluación", "/admin/evaluaciones/guardar");
        return "admin/evaluaciones/form";
    }

    @PostMapping("/admin/evaluaciones/guardar")
    public String guardarAdmin(@Valid @ModelAttribute("evaluacionForm") EvaluacionPracticanteFormDTO dto,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            cargarFormulario(model, dto, "Nueva evaluación", "/admin/evaluaciones/guardar");
            return "admin/evaluaciones/form";
        }
        try {
            evaluacionPracticanteService.crear(dto, null, true);
            redirectAttributes.addFlashAttribute("toastMensaje", "Evaluación creada correctamente");
            redirectAttributes.addFlashAttribute("toastTipo", "success");
            return "redirect:/admin/evaluaciones";
        } catch (IllegalArgumentException ex) {
            cargarFormulario(model, dto, "Nueva evaluación", "/admin/evaluaciones/guardar");
            model.addAttribute("errorGeneral", ex.getMessage());
            return "admin/evaluaciones/form";
        }
    }

    @GetMapping("/admin/evaluaciones/editar/{id}")
    public String editarAdmin(@PathVariable Long id, Model model) {
        var evaluacion = evaluacionPracticanteService.obtenerPorId(id);
        EvaluacionPracticanteFormDTO dto = new EvaluacionPracticanteFormDTO();
        dto.setPracticanteId(evaluacion.getPracticante().getId());
        dto.setDoctorSupervisorId(evaluacion.getDoctorSupervisor().getId());
        dto.setFechaEvaluacion(evaluacion.getFechaEvaluacion());
        dto.setPuntaje(evaluacion.getPuntaje());
        dto.setComentario(evaluacion.getComentario());
        dto.setEstado(evaluacion.getEstado());
        cargarFormulario(model, dto, "Editar evaluación", "/admin/evaluaciones/actualizar/" + id);
        return "admin/evaluaciones/form";
    }

    @PostMapping("/admin/evaluaciones/actualizar/{id}")
    public String actualizarAdmin(@PathVariable Long id,
                                  @Valid @ModelAttribute("evaluacionForm") EvaluacionPracticanteFormDTO dto,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            cargarFormulario(model, dto, "Editar evaluación", "/admin/evaluaciones/actualizar/" + id);
            return "admin/evaluaciones/form";
        }
        try {
            evaluacionPracticanteService.actualizar(id, dto);
            redirectAttributes.addFlashAttribute("toastMensaje", "Evaluación actualizada correctamente");
            redirectAttributes.addFlashAttribute("toastTipo", "success");
            return "redirect:/admin/evaluaciones";
        } catch (IllegalArgumentException ex) {
            cargarFormulario(model, dto, "Editar evaluación", "/admin/evaluaciones/actualizar/" + id);
            model.addAttribute("errorGeneral", ex.getMessage());
            return "admin/evaluaciones/form";
        }
    }

    @PostMapping("/admin/evaluaciones/eliminar/{id}")
    public String eliminarAdmin(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        evaluacionPracticanteService.eliminar(id);
        redirectAttributes.addFlashAttribute("toastMensaje", "Evaluación eliminada correctamente");
        redirectAttributes.addFlashAttribute("toastTipo", "success");
        return "redirect:/admin/evaluaciones";
    }

    @GetMapping("/doctor/evaluaciones")
    public String indexDoctor(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        var doctor = doctorService.obtenerPorCorreo(userDetails.getUsername());
        model.addAttribute("pageTitle", "Evaluaciones");
        model.addAttribute("pageDescription", "Evaluaciones realizadas a tus practicantes.");
        model.addAttribute("evaluaciones", evaluacionPracticanteService.listarPorDoctor(doctor.getId()));
        return "doctor/evaluar-practicante";
    }

    @GetMapping("/doctor/evaluaciones/nueva/{idPracticante}")
    public String nuevaDoctor(@PathVariable Long idPracticante,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              Model model) {
        var doctor = doctorService.obtenerPorCorreo(userDetails.getUsername());
        EvaluacionPracticanteFormDTO dto = new EvaluacionPracticanteFormDTO();
        dto.setPracticanteId(idPracticante);
        dto.setDoctorSupervisorId(doctor.getId());
        dto.setFechaEvaluacion(LocalDate.now());
        dto.setEstado(true);
        cargarFormularioDoctor(model, dto);
        return "doctor/evaluar-practicante";
    }

    @PostMapping("/doctor/evaluaciones/guardar")
    public String guardarDoctor(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @Valid @ModelAttribute("evaluacionForm") EvaluacionPracticanteFormDTO dto,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        var doctor = doctorService.obtenerPorCorreo(userDetails.getUsername());
        if (bindingResult.hasErrors()) {
            cargarFormularioDoctor(model, dto);
            return "doctor/evaluar-practicante";
        }
        try {
            evaluacionPracticanteService.crear(dto, doctor.getId(), false);
            redirectAttributes.addFlashAttribute("toastMensaje", "Evaluación registrada correctamente");
            redirectAttributes.addFlashAttribute("toastTipo", "success");
            return "redirect:/doctor/evaluaciones";
        } catch (IllegalArgumentException ex) {
            cargarFormularioDoctor(model, dto);
            model.addAttribute("errorGeneral", ex.getMessage());
            return "doctor/evaluar-practicante";
        }
    }

    @GetMapping("/practicante/mis-evaluaciones")
    public String misEvaluaciones(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        var practicante = practicanteService.obtenerPorCorreo(userDetails.getUsername());
        model.addAttribute("pageTitle", "Mis Evaluaciones");
        model.addAttribute("pageDescription", "Historial de evaluaciones recibidas.");
        model.addAttribute("promedio", evaluacionPracticanteService.promedioPorPracticante(practicante.getId()));
        model.addAttribute("evaluaciones", evaluacionPracticanteService.listarPorPracticante(practicante.getId()));
        return "practicante/mis-evaluaciones";
    }

    private void cargarFormulario(Model model, EvaluacionPracticanteFormDTO dto, String titulo, String action) {
        model.addAttribute("pageTitle", titulo);
        model.addAttribute("pageDescription", "Formulario de evaluación.");
        model.addAttribute("evaluacionForm", dto);
        model.addAttribute("practicantes", practicanteService.listar(null, null, true));
        model.addAttribute("doctores", doctorService.listar(null, null, true));
        model.addAttribute("formAction", action);
    }

    private void cargarFormularioDoctor(Model model, EvaluacionPracticanteFormDTO dto) {
        model.addAttribute("pageTitle", "Evaluar Practicante");
        model.addAttribute("pageDescription", "Registra una evaluación académica.");
        model.addAttribute("evaluacionForm", dto);
        model.addAttribute("practicantes", practicanteService.listar(null, dto.getDoctorSupervisorId(), true));
        model.addAttribute("formAction", "/doctor/evaluaciones/guardar");
    }
}
