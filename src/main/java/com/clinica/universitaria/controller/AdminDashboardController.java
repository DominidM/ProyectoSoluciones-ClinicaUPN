package com.clinica.universitaria.controller;

import com.clinica.universitaria.service.AtencionClinicaService;
import com.clinica.universitaria.service.EvaluacionPracticanteService;
import com.clinica.universitaria.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AdminDashboardController {

    private final ReporteService reporteService;
    private final AtencionClinicaService atencionClinicaService;
    private final EvaluacionPracticanteService evaluacionPracticanteService;

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        Map<String, Long> metricas = reporteService.metricasGenerales();
        Map<String, Long> citasPorEspecialidad = reporteService.citasPorEspecialidad();
        Map<String, Long> estadoCitasBase = reporteService.estadoCitas();
        List<Map<String, Object>> barrasEspecialidad = construirBarrasEspecialidad(citasPorEspecialidad);
        List<Map<String, Object>> estadoCitas = construirEstadoCitas(estadoCitasBase);

        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("pageDescription", "Resumen operativo y académico de la clínica.");
        model.addAttribute("metricas", metricas);
        model.addAttribute("proximasCitas", reporteService.proximasCitas());
        model.addAttribute("ultimasAtenciones", atencionClinicaService.recientes());
        model.addAttribute("evaluacionesRecientes", evaluacionPracticanteService.recientes());
        model.addAttribute("barrasEspecialidad", barrasEspecialidad);
        model.addAttribute("estadoCitas", estadoCitas);
        model.addAttribute("estadoCitasDonutStyle", construirDonutStyle(estadoCitas));
        model.addAttribute("atencionesPorDoctor", reporteService.atencionesPorDoctor());
        return "admin/dashboard";
    }

    private List<Map<String, Object>> construirBarrasEspecialidad(Map<String, Long> citasPorEspecialidad) {
        long maximo = citasPorEspecialidad.values().stream().mapToLong(Long::longValue).max().orElse(1L);
        List<Map<String, Object>> barras = new ArrayList<>();
        citasPorEspecialidad.forEach((nombre, valor) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            long altura = maximo == 0 ? 12 : Math.max(12L, Math.round((valor * 100.0) / maximo));
            item.put("nombre", nombre);
            item.put("etiqueta", abreviarEspecialidad(nombre));
            item.put("valor", valor);
            item.put("altura", altura + "%");
            barras.add(item);
        });
        return barras;
    }

    private List<Map<String, Object>> construirEstadoCitas(Map<String, Long> estadoCitasBase) {
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(crearEstadoItem("Confirmada", estadoCitasBase.getOrDefault("CONFIRMADA", 0L), "#3B82F6"));
        items.add(crearEstadoItem("Atendida", estadoCitasBase.getOrDefault("ATENDIDA", 0L), "#10B981"));
        items.add(crearEstadoItem("Pendiente", estadoCitasBase.getOrDefault("PENDIENTE", 0L), "#F5C518"));
        items.add(crearEstadoItem("Cancelada", estadoCitasBase.getOrDefault("CANCELADA", 0L), "#EF4444"));
        items.add(crearEstadoItem("Reprogramada", estadoCitasBase.getOrDefault("REPROGRAMADA", 0L), "#A855F7"));

        long total = items.stream()
                .mapToLong(item -> (Long) item.get("valor"))
                .sum();

        items.forEach(item -> {
            long valor = (Long) item.get("valor");
            long porcentaje = total == 0 ? 0L : Math.round((valor * 100.0) / total);
            item.put("porcentaje", porcentaje + "%");
        });
        return items;
    }

    private Map<String, Object> crearEstadoItem(String nombre, Long valor, String color) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("nombre", nombre);
        item.put("valor", valor);
        item.put("color", color);
        return item;
    }

    private String construirDonutStyle(List<Map<String, Object>> estadoCitas) {
        long total = estadoCitas.stream()
                .mapToLong(item -> (Long) item.get("valor"))
                .sum();
        if (total == 0) {
            return "background: conic-gradient(#E5E7EB 0% 100%);";
        }

        double inicio = 0;
        StringBuilder style = new StringBuilder("background: conic-gradient(");
        boolean agregado = false;

        for (Map<String, Object> item : estadoCitas) {
            long valor = (Long) item.get("valor");
            if (valor <= 0) {
                continue;
            }
            double fin = inicio + (valor * 100.0 / total);
            if (agregado) {
                style.append(", ");
            }
            style.append(item.get("color"))
                    .append(' ')
                    .append(String.format(java.util.Locale.US, "%.2f", inicio))
                    .append("% ")
                    .append(String.format(java.util.Locale.US, "%.2f", fin))
                    .append('%');
            inicio = fin;
            agregado = true;
        }

        if (inicio < 100) {
            if (agregado) {
                style.append(", ");
            }
            style.append("#E5E7EB ")
                    .append(String.format(java.util.Locale.US, "%.2f", inicio))
                    .append("% 100%");
        }
        style.append(");");
        return style.toString();
    }

    private String abreviarEspecialidad(String nombre) {
        return switch (nombre) {
            case "Medicina General" -> "Med. General";
            case "Rehabilitación" -> "Rehabilitación";
            default -> nombre;
        };
    }
}
