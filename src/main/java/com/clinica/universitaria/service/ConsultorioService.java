package com.clinica.universitaria.service;

import com.clinica.universitaria.dto.ConsultorioFormDTO;
import com.clinica.universitaria.model.Consultorio;
import com.clinica.universitaria.model.EstadoCita;
import com.clinica.universitaria.repository.CitaRepository;
import com.clinica.universitaria.repository.ConsultorioRepository;
import com.clinica.universitaria.repository.EspecialidadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultorioService {

    private final ConsultorioRepository consultorioRepository;
    private final EspecialidadRepository especialidadRepository;
    private final CitaRepository citaRepository;

    public List<Consultorio> listar(String q, Long especialidadId, Boolean estado) {
        return consultorioRepository.buscar(texto(q), especialidadId, estado);
    }

    public Consultorio obtenerPorId(Long id) {
        return consultorioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Consultorio no encontrado"));
    }

    public long contarActivos() {
        return consultorioRepository.countByEstadoTrue();
    }

    @Transactional
    public Consultorio crear(ConsultorioFormDTO dto) {
        return consultorioRepository.save(Consultorio.builder()
                .especialidad(especialidadRepository.findById(dto.getEspecialidadId())
                        .orElseThrow(() -> new IllegalArgumentException("Especialidad no encontrada")))
                .nombre(dto.getNombre())
                .numero(dto.getNumero())
                .piso(dto.getPiso())
                .descripcion(dto.getDescripcion())
                .estado(dto.getEstado())
                .build());
    }

    @Transactional
    public Consultorio actualizar(Long id, ConsultorioFormDTO dto) {
        Consultorio consultorio = obtenerPorId(id);
        consultorio.setEspecialidad(especialidadRepository.findById(dto.getEspecialidadId())
                .orElseThrow(() -> new IllegalArgumentException("Especialidad no encontrada")));
        consultorio.setNombre(dto.getNombre());
        consultorio.setNumero(dto.getNumero());
        consultorio.setPiso(dto.getPiso());
        consultorio.setDescripcion(dto.getDescripcion());
        consultorio.setEstado(dto.getEstado());
        return consultorioRepository.save(consultorio);
    }

    @Transactional
    public void desactivar(Long id) {
        Consultorio consultorio = obtenerPorId(id);
        boolean tieneCitasActivas = citaRepository.findAll().stream()
                .anyMatch(cita -> cita.getConsultorio().getId().equals(id)
                        && Set.of(EstadoCita.PENDIENTE, EstadoCita.CONFIRMADA, EstadoCita.REPROGRAMADA).contains(cita.getEstado()));
        if (tieneCitasActivas) {
            throw new IllegalArgumentException("No se puede desactivar el consultorio porque tiene citas activas");
        }
        consultorio.setEstado(false);
        consultorioRepository.save(consultorio);
    }

    private String texto(String q) {
        return StringUtils.hasText(q) ? q.trim() : null;
    }
}
