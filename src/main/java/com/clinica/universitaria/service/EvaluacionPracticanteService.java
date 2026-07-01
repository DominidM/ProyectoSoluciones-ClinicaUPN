package com.clinica.universitaria.service;

import com.clinica.universitaria.dto.EvaluacionPracticanteFormDTO;
import com.clinica.universitaria.model.Doctor;
import com.clinica.universitaria.model.EvaluacionPracticante;
import com.clinica.universitaria.model.Practicante;
import com.clinica.universitaria.repository.DoctorRepository;
import com.clinica.universitaria.repository.EvaluacionPracticanteRepository;
import com.clinica.universitaria.repository.PracticanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EvaluacionPracticanteService {

    private final EvaluacionPracticanteRepository evaluacionPracticanteRepository;
    private final PracticanteRepository practicanteRepository;
    private final DoctorRepository doctorRepository;

    public List<EvaluacionPracticante> listarTodas() {
        return evaluacionPracticanteRepository.buscar(null, null, null);
    }

    public List<EvaluacionPracticante> listarPorDoctor(Long doctorId) {
        return evaluacionPracticanteRepository.buscar(doctorId, null, true);
    }

    public List<EvaluacionPracticante> listarPorPracticante(Long practicanteId) {
        return evaluacionPracticanteRepository.buscar(null, practicanteId, true);
    }

    public List<EvaluacionPracticante> recientes() {
        return evaluacionPracticanteRepository.findTop8ByOrderByFechaEvaluacionDesc();
    }

    public BigDecimal promedioPorPracticante(Long practicanteId) {
        return evaluacionPracticanteRepository.promedioPorPracticante(practicanteId);
    }

    public EvaluacionPracticante obtenerPorId(Long id) {
        return evaluacionPracticanteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evaluación no encontrada"));
    }

    @Transactional
    public EvaluacionPracticante crear(EvaluacionPracticanteFormDTO dto, Long doctorActualId, boolean admin) {
        Practicante practicante = practicanteRepository.findById(dto.getPracticanteId())
                .orElseThrow(() -> new IllegalArgumentException("Practicante no encontrado"));
        Doctor doctorSupervisor = doctorRepository.findById(dto.getDoctorSupervisorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor supervisor no encontrado"));
        validar(dto, practicante, doctorSupervisor, doctorActualId, admin);
        return evaluacionPracticanteRepository.save(EvaluacionPracticante.builder()
                .practicante(practicante)
                .doctorSupervisor(doctorSupervisor)
                .fechaEvaluacion(dto.getFechaEvaluacion())
                .puntaje(dto.getPuntaje())
                .comentario(dto.getComentario())
                .estado(dto.getEstado())
                .build());
    }

    @Transactional
    public EvaluacionPracticante actualizar(Long id, EvaluacionPracticanteFormDTO dto) {
        EvaluacionPracticante evaluacionPracticante = obtenerPorId(id);
        Practicante practicante = practicanteRepository.findById(dto.getPracticanteId())
                .orElseThrow(() -> new IllegalArgumentException("Practicante no encontrado"));
        Doctor doctorSupervisor = doctorRepository.findById(dto.getDoctorSupervisorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor supervisor no encontrado"));
        validar(dto, practicante, doctorSupervisor, null, true);
        evaluacionPracticante.setPracticante(practicante);
        evaluacionPracticante.setDoctorSupervisor(doctorSupervisor);
        evaluacionPracticante.setFechaEvaluacion(dto.getFechaEvaluacion());
        evaluacionPracticante.setPuntaje(dto.getPuntaje());
        evaluacionPracticante.setComentario(dto.getComentario());
        evaluacionPracticante.setEstado(dto.getEstado());
        return evaluacionPracticanteRepository.save(evaluacionPracticante);
    }

    @Transactional
    public void eliminar(Long id) {
        EvaluacionPracticante evaluacionPracticante = obtenerPorId(id);
        evaluacionPracticante.setEstado(false);
        evaluacionPracticanteRepository.save(evaluacionPracticante);
    }

    private void validar(EvaluacionPracticanteFormDTO dto, Practicante practicante, Doctor doctorSupervisor, Long doctorActualId, boolean admin) {
        if (dto.getPuntaje().compareTo(BigDecimal.ZERO) < 0 || dto.getPuntaje().compareTo(BigDecimal.valueOf(20)) > 0) {
            throw new IllegalArgumentException("El puntaje debe estar entre 0 y 20");
        }
        if (!admin && doctorActualId != null) {
            if (!doctorSupervisor.getId().equals(doctorActualId)) {
                throw new IllegalArgumentException("Solo puedes evaluar con tu propio usuario supervisor");
            }
            if (practicante.getDoctorSupervisor() == null || !practicante.getDoctorSupervisor().getId().equals(doctorActualId)) {
                throw new IllegalArgumentException("Solo puedes evaluar practicantes asignados a ti");
            }
        }
    }
}
