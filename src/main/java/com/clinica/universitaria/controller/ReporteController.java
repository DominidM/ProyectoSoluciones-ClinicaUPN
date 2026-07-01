package com.clinica.universitaria.controller;

import com.clinica.universitaria.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/admin/reportes")
    public String index(@RequestParam(required = false) LocalDate desde,
                        @RequestParam(required = false) LocalDate hasta,
                        Model model) {
        model.addAttribute("pageTitle", "Reportes");
        model.addAttribute("pageDescription", "Estadísticas administrativas y métricas operativas.");
        model.addAttribute("desde", desde != null ? desde : LocalDate.now().withDayOfMonth(1));
        model.addAttribute("hasta", hasta != null ? hasta : LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()));
        model.addAttribute("metricas", reporteService.metricasGenerales());
        model.addAttribute("citasPorEspecialidad", reporteService.citasPorEspecialidad());
        model.addAttribute("estadoCitas", reporteService.estadoCitas());
        model.addAttribute("atencionesPorDoctor", reporteService.atencionesPorDoctor());
        model.addAttribute("atencionesPorMes", reporteService.atencionesPorMes());
        model.addAttribute("consultoriosMasUsados", reporteService.consultoriosMasUsados());
        model.addAttribute("distribucionEvaluaciones", reporteService.distribucionEvaluaciones());
        model.addAttribute("evaluacionesRecientes", reporteService.evaluacionesRecientes());
        return "admin/reportes/index";
    }
}
