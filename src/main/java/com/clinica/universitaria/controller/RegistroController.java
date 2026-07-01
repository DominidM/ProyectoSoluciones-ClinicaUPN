package com.clinica.universitaria.controller;

import com.clinica.universitaria.dto.RegistroPacienteDTO;
import com.clinica.universitaria.service.RegistroPacienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class RegistroController {

    private final RegistroPacienteService registroPacienteService;

    @GetMapping("/registro")
    public String form(Model model) {
        model.addAttribute("pageTitle", "Registro de paciente");
        model.addAttribute("registroPacienteDTO", new RegistroPacienteDTO());
        return "registro/paciente";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registroPacienteDTO") RegistroPacienteDTO dto,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        model.addAttribute("pageTitle", "Registro de paciente");
        if (bindingResult.hasErrors()) {
            return "registro/paciente";
        }
        try {
            registroPacienteService.registrar(dto);
            redirectAttributes.addFlashAttribute("toastMensaje", "Cuenta creada correctamente. Ahora puedes iniciar sesión.");
            redirectAttributes.addFlashAttribute("toastTipo", "success");
            return "redirect:/registro/exito";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorGeneral", ex.getMessage());
            return "registro/paciente";
        }
    }

    @GetMapping("/registro/exito")
    public String exito(Model model) {
        model.addAttribute("pageTitle", "Registro exitoso");
        return "registro/exito";
    }
}
