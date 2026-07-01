package com.clinica.universitaria.service;

import com.clinica.universitaria.dto.AtencionClinicaFormDTO;
import com.clinica.universitaria.model.AtencionClinica;
import com.clinica.universitaria.model.Cita;
import com.clinica.universitaria.model.EstadoCita;
import com.clinica.universitaria.model.HistoriaClinica;
import com.clinica.universitaria.model.Practicante;
import com.clinica.universitaria.repository.AtencionClinicaRepository;
import com.clinica.universitaria.repository.CitaRepository;
import com.clinica.universitaria.repository.PracticanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AtencionClinicaService {

    private final AtencionClinicaRepository atencionClinicaRepository;
    private final CitaRepository citaRepository;
    private final PracticanteRepository practicanteRepository;
    private final HistoriaClinicaService historiaClinicaService;

    public List<AtencionClinica> listarTodas() {
        return atencionClinicaRepository.findAll().stream()
                .sorted((a, b) -> b.getFechaAtencion().compareTo(a.getFechaAtencion()))
                .toList();
    }

    public List<AtencionClinica> listarPorDoctor(Long doctorId) {
        return atencionClinicaRepository.buscar(doctorId, null);
    }

    public List<AtencionClinica> listarPorPracticante(Long practicanteId) {
        return atencionClinicaRepository.buscar(null, practicanteId);
    }

    public List<AtencionClinica> recientes() {
        return atencionClinicaRepository.findTop8ByOrderByFechaAtencionDesc();
    }

    public AtencionClinica obtenerPorId(Long id) {
        return atencionClinicaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Atención no encontrada"));
    }

    @Transactional
    public AtencionClinica registrar(AtencionClinicaFormDTO dto, Long doctorActualId, boolean admin) {
        Cita cita = citaRepository.findById(dto.getCitaId())
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));
        if (!admin && !cita.getDoctor().getId().equals(doctorActualId)) {
            throw new IllegalArgumentException("No puedes registrar una atención para una cita que no te pertenece");
        }
        if (cita.getAtencionClinica() != null) {
            throw new IllegalArgumentException("La cita ya tiene una atención registrada");
        }
        HistoriaClinica historiaClinica = historiaClinicaService.obtenerPorPaciente(cita.getPaciente());
        Practicante practicante = dto.getPracticanteId() == null ? null :
                practicanteRepository.findById(dto.getPracticanteId())
                        .orElseThrow(() -> new IllegalArgumentException("Practicante no encontrado"));
        if (practicante != null && !Boolean.TRUE.equals(practicante.getEstado())) {
            throw new IllegalArgumentException("El practicante seleccionado no está activo");
        }
        if (!admin && practicante != null
                && (practicante.getDoctorSupervisor() == null
                || !practicante.getDoctorSupervisor().getId().equals(doctorActualId))) {
            throw new IllegalArgumentException("Solo puedes registrar practicantes asignados a ti");
        }

        AtencionClinica atencionClinica = AtencionClinica.builder()
                .cita(cita)
                .historiaClinica(historiaClinica)
                .doctor(cita.getDoctor())
                .practicante(practicante)
                .motivoConsulta(dto.getMotivoConsulta())
                .diagnostico(dto.getDiagnostico())
                .tratamiento(dto.getTratamiento())
                .observaciones(dto.getObservaciones())
                .build();
        atencionClinica = atencionClinicaRepository.save(atencionClinica);
        cita.setEstado(EstadoCita.ATENDIDA);
        citaRepository.save(cita);
        return atencionClinica;
    }
}
