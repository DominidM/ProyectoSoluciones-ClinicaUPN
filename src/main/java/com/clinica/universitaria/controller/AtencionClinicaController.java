package com.clinica.universitaria.controller;

import com.clinica.universitaria.dto.AtencionClinicaFormDTO;
import com.clinica.universitaria.service.AtencionClinicaService;
import com.clinica.universitaria.service.CitaService;
import com.clinica.universitaria.service.DoctorService;
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
public class AtencionClinicaController {

    private final AtencionClinicaService atencionClinicaService;
    private final CitaService citaService;
    private final DoctorService doctorService;
    private final PracticanteService practicanteService;

    @GetMapping("/admin/atenciones")
    public String indexAdmin(Model model) {
        model.addAttribute("pageTitle", "Atenciones Clínicas");
        model.addAttribute("pageDescription", "Registro de todas las atenciones médicas.");
        model.addAttribute("atenciones", atencionClinicaService.listarTodas());
        return "admin/atenciones/index";
    }

    @GetMapping("/admin/atenciones/nueva/{idCita}")
    public String nuevaAdmin(@PathVariable Long idCita, Model model) {
        AtencionClinicaFormDTO dto = new AtencionClinicaFormDTO();
        dto.setCitaId(idCita);
        cargarFormulario(model, dto, "Registrar atención", "/admin/atenciones/guardar", idCita);
        return "admin/atenciones/form";
    }

    @PostMapping("/admin/atenciones/guardar")
    public String guardarAdmin(@Valid @ModelAttribute("atencionForm") AtencionClinicaFormDTO dto,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            cargarFormulario(model, dto, "Registrar atención", "/admin/atenciones/guardar", dto.getCitaId());
            return "admin/atenciones/form";
        }
        try {
            atencionClinicaService.registrar(dto, null, true);
            redirectAttributes.addFlashAttribute("toastMensaje", "Atención registrada correctamente");
            redirectAttributes.addFlashAttribute("toastTipo", "success");
            return "redirect:/admin/atenciones";
        } catch (IllegalArgumentException ex) {
            cargarFormulario(model, dto, "Registrar atención", "/admin/atenciones/guardar", dto.getCitaId());
            model.addAttribute("errorGeneral", ex.getMessage());
            return "admin/atenciones/form";
        }
    }

    @GetMapping("/admin/atenciones/ver/{id}")
    public String verAdmin(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Detalle de Atención");
        model.addAttribute("pageDescription", "Información completa de la atención.");
        model.addAttribute("atencion", atencionClinicaService.obtenerPorId(id));
        return "admin/atenciones/detail";
    }

    @GetMapping("/doctor/atenciones")
    public String indexDoctor(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        var doctor = doctorService.obtenerPorCorreo(userDetails.getUsername());
        model.addAttribute("pageTitle", "Atenciones Clínicas");
        model.addAttribute("pageDescription", "Atenciones registradas por ti.");
        model.addAttribute("atenciones", atencionClinicaService.listarPorDoctor(doctor.getId()));
        return "doctor/atenciones";
    }

    @GetMapping("/doctor/atenciones/nueva/{idCita}")
    public String nuevaDoctor(@PathVariable Long idCita,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              Model model) {
        var doctor = doctorService.obtenerPorCorreo(userDetails.getUsername());
        AtencionClinicaFormDTO dto = new AtencionClinicaFormDTO();
        dto.setCitaId(idCita);
        cargarFormulario(model, dto, "Registrar atención clínica", "/doctor/atenciones/guardar", idCita, doctor.getId());
        return "doctor/registrar-atencion";
    }

    @PostMapping("/doctor/atenciones/guardar")
    public String guardarDoctor(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @Valid @ModelAttribute("atencionForm") AtencionClinicaFormDTO dto,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        var doctor = doctorService.obtenerPorCorreo(userDetails.getUsername());
        if (bindingResult.hasErrors()) {
            cargarFormulario(model, dto, "Registrar atención clínica", "/doctor/atenciones/guardar", dto.getCitaId(), doctor.getId());
            return "doctor/registrar-atencion";
        }
        try {
            atencionClinicaService.registrar(dto, doctor.getId(), false);
            redirectAttributes.addFlashAttribute("toastMensaje", "Atención registrada correctamente");
            redirectAttributes.addFlashAttribute("toastTipo", "success");
            return "redirect:/doctor/atenciones";
        } catch (IllegalArgumentException ex) {
            cargarFormulario(model, dto, "Registrar atención clínica", "/doctor/atenciones/guardar", dto.getCitaId(), doctor.getId());
            model.addAttribute("errorGeneral", ex.getMessage());
            return "doctor/registrar-atencion";
        }
    }

    @GetMapping("/practicante/atenciones-participadas")
    public String atencionesParticipadas(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        var practicante = practicanteService.obtenerPorCorreo(userDetails.getUsername());
        model.addAttribute("pageTitle", "Atenciones donde Participé");
        model.addAttribute("pageDescription", "Registro de atenciones clínicas asociadas a tu participación.");
        model.addAttribute("atenciones", atencionClinicaService.listarPorPracticante(practicante.getId()));
        return "practicante/atenciones-participadas";
    }

    private void cargarFormulario(Model model, AtencionClinicaFormDTO dto, String titulo, String action, Long citaId) {
        cargarFormulario(model, dto, titulo, action, citaId, null);
    }

    private void cargarFormulario(Model model, AtencionClinicaFormDTO dto, String titulo, String action,
                                  Long citaId, Long doctorSupervisorId) {
        model.addAttribute("pageTitle", titulo);
        model.addAttribute("pageDescription", "Formulario de atención clínica.");
        model.addAttribute("atencionForm", dto);
        model.addAttribute("cita", citaService.obtenerPorId(citaId));
        model.addAttribute("practicantes", practicanteService.listar(null, doctorSupervisorId, true));
        model.addAttribute("formAction", action);
    }
}
