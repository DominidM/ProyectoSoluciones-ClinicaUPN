package com.clinica.universitaria.service;

import com.clinica.universitaria.model.Cita;
import com.clinica.universitaria.model.EstadoCita;
import com.clinica.universitaria.model.EvaluacionPracticante;
import com.clinica.universitaria.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteService {

    private final PacienteRepository pacienteRepository;
    private final DoctorRepository doctorRepository;
    private final PracticanteRepository practicanteRepository;
    private final ConsultorioRepository consultorioRepository;
    private final CitaRepository citaRepository;
    private final AtencionClinicaRepository atencionClinicaRepository;
    private final EvaluacionPracticanteRepository evaluacionPracticanteRepository;

    public Map<String, Long> metricasGenerales() {
        Map<String, Long> metricas = new LinkedHashMap<>();
        metricas.put("pacientes", pacienteRepository.countByEstadoTrue());
        metricas.put("doctores", doctorRepository.countByEstadoTrue());
        metricas.put("practicantes", practicanteRepository.countByEstadoTrue());
        metricas.put("consultorios", consultorioRepository.countByEstadoTrue());
        metricas.put("citasHoy", citaRepository.countByFechaCita(LocalDate.now()));
        metricas.put("citasPendientes", citaRepository.countByEstado(EstadoCita.PENDIENTE));
        metricas.put("citasConfirmadas", citaRepository.countByEstado(EstadoCita.CONFIRMADA));
        metricas.put("citasAtendidas", citaRepository.countByEstado(EstadoCita.ATENDIDA));
        metricas.put("citasCanceladas", citaRepository.countByEstado(EstadoCita.CANCELADA));
        metricas.put("totalAtenciones", (long) atencionClinicaRepository.findAll().size());
        return metricas;
    }

    public Map<String, Long> citasPorEspecialidad() {
        return citaRepository.findAll().stream()
                .collect(Collectors.groupingBy(c -> c.getEspecialidad().getNombre(), LinkedHashMap::new, Collectors.counting()));
    }

    public Map<String, Long> estadoCitas() {
        return citaRepository.findAll().stream()
                .collect(Collectors.groupingBy(c -> c.getEstado().name(), LinkedHashMap::new, Collectors.counting()));
    }

    public Map<String, Long> atencionesPorDoctor() {
        return atencionClinicaRepository.findAll().stream()
                .collect(Collectors.groupingBy(a -> a.getDoctor().getUsuario().getNombreCompleto(),
                        LinkedHashMap::new, Collectors.counting()));
    }

    public Map<String, Long> consultoriosMasUsados() {
        return citaRepository.findAll().stream()
                .collect(Collectors.groupingBy(c -> c.getConsultorio().getNombre(), LinkedHashMap::new, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(6)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    public Map<String, Long> atencionesPorMes() {
        Locale locale = Locale.forLanguageTag("es-PE");
        return atencionClinicaRepository.findAll().stream()
                .sorted(Comparator.comparing(a -> a.getFechaAtencion().toLocalDate()))
                .collect(Collectors.groupingBy(a -> a.getFechaAtencion().getMonth()
                                .getDisplayName(TextStyle.SHORT, locale),
                        LinkedHashMap::new,
                        Collectors.counting()));
    }

    public Map<String, Long> distribucionEvaluaciones() {
        return evaluacionPracticanteRepository.findAll().stream()
                .collect(Collectors.groupingBy(this::rangoEvaluacion, LinkedHashMap::new, Collectors.counting()));
    }

    public List<EvaluacionPracticante> evaluacionesRecientes() {
        return evaluacionPracticanteRepository.findTop8ByOrderByFechaEvaluacionDesc();
    }

    public List<Cita> proximasCitas() {
        return citaRepository.findTop8ByFechaCitaGreaterThanEqualOrderByFechaCitaAscHoraInicioAsc(LocalDate.now());
    }

    private String rangoEvaluacion(EvaluacionPracticante evaluacion) {
        double valor = evaluacion.getPuntaje().doubleValue();
        if (valor >= 18) {
            return "18-20 (Excelente)";
        }
        if (valor >= 14) {
            return "14-17 (Bueno)";
        }
        if (valor >= 11) {
            return "11-13 (Regular)";
        }
        return "0-10 (Deficiente)";
    }
}
