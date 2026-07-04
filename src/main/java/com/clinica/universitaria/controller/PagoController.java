package com.clinica.universitaria.controller;

import com.clinica.universitaria.model.Paciente;
import com.clinica.universitaria.service.PacienteService;
import com.clinica.universitaria.service.PagoService;
import com.clinica.universitaria.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;
    private final PacienteService pacienteService;

    @GetMapping("/paciente/mis-pagos")
    public String misPagos(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Paciente paciente = pacienteService.obtenerPorCorreo(userDetails.getUsername());
        model.addAttribute("pageTitle", "Mis Pagos");
        model.addAttribute("pageDescription", "Gestion de pagos de citas.");
        model.addAttribute("paciente", paciente);
        model.addAttribute("pagos", pagoService.obtenerPorPaciente(paciente.getId()));
        return "paciente/mis-pagos";
    }

    @PostMapping("/paciente/pagar-cita/{citaId}")
    public String iniciarPago(@PathVariable Long citaId, Model model) {
        String initPoint = pagoService.getInitPoint(citaId);
        if (initPoint == null) {
            return "redirect:/paciente/mis-pagos?error=No se pudo generar el pago";
        }
        return "redirect:" + initPoint;
    }

    @GetMapping("/pagos/exito")
    public String pagoExito(@RequestParam(required = false) String preference_id,
                            @RequestParam(required = false) String collection_status) {
        return "pagos/resultado";
    }

    @GetMapping("/pagos/fallo")
    public String pagoFallo() {
        return "pagos/resultado";
    }

    @GetMapping("/pagos/pendiente")
    public String pagoPendiente() {
        return "pagos/resultado";
    }

    @PostMapping("/api/pagos/webhook")
    @ResponseBody
    public Map<String, String> webhook(@RequestBody Map<String, Object> body) {
        String preferenceId = (String) body.get("preference_id");
        if (preferenceId != null) {
            pagoService.procesarNotificacion(preferenceId);
        }
        return Map.of("status", "ok");
    }
}
