package com.clinica.universitaria.controller;

import com.clinica.universitaria.service.EspecialidadService;
import com.clinica.universitaria.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PublicController {

    private final EspecialidadService especialidadService;
    private final ReporteService reporteService;

    @GetMapping("/")
    public String landing(Model model) {
        model.addAttribute("pageTitle", "Inicio");
        model.addAttribute("especialidades", especialidadService.listarActivas());
        model.addAttribute("metricas", reporteService.metricasGenerales());
        return "public/landing";
    }

    @GetMapping("/especialidades")
    public String especialidades(Model model) {
        model.addAttribute("pageTitle", "Especialidades");
        model.addAttribute("especialidades", especialidadService.listarActivas());
        return "public/especialidades";
    }

    @GetMapping("/sobre-clinica")
    public String sobreClinica(Model model) {
        model.addAttribute("pageTitle", "Sobre la Clínica");
        model.addAttribute("metricas", reporteService.metricasGenerales());
        return "public/sobre-clinica";
    }
}
