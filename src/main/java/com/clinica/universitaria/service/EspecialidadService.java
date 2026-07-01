package com.clinica.universitaria.service;

import com.clinica.universitaria.dto.EspecialidadFormDTO;
import com.clinica.universitaria.model.Especialidad;
import com.clinica.universitaria.repository.EspecialidadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EspecialidadService {

    private final EspecialidadRepository especialidadRepository;

    public List<Especialidad> listarTodas() {
        return especialidadRepository.findAll().stream()
                .sorted((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()))
                .toList();
    }

    public List<Especialidad> listarActivas() {
        return especialidadRepository.findByEstadoTrueOrderByNombreAsc();
    }

    public Especialidad obtenerPorId(Long id) {
        return especialidadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Especialidad no encontrada"));
    }

    @Transactional
    public Especialidad crear(EspecialidadFormDTO dto) {
        validarNombre(dto.getNombre(), null);
        validarDescripcion(dto.getDescripcion());
        return especialidadRepository.save(Especialidad.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .estado(dto.getEstado())
                .build());
    }

    @Transactional
    public Especialidad actualizar(Long id, EspecialidadFormDTO dto) {
        Especialidad especialidad = obtenerPorId(id);
        validarNombre(dto.getNombre(), id);
        validarDescripcion(dto.getDescripcion());
        especialidad.setNombre(dto.getNombre());
        especialidad.setDescripcion(dto.getDescripcion());
        especialidad.setEstado(dto.getEstado());
        return especialidadRepository.save(especialidad);
    }

    @Transactional
    public void desactivar(Long id) {
        Especialidad especialidad = obtenerPorId(id);
        especialidad.setEstado(false);
        especialidadRepository.save(especialidad);
    }

    private void validarNombre(String nombre, Long excluirId) {
        if (nombre != null && nombre.length() > 100) {
            throw new IllegalArgumentException("El nombre no puede superar los 100 caracteres");
        }
        String nombreNormalizado = nombre == null ? null : nombre.trim();
        boolean existe = especialidadRepository.findAll().stream()
                .filter(e -> excluirId == null || !e.getId().equals(excluirId))
                .anyMatch(e -> e.getNombre().equalsIgnoreCase(nombreNormalizado));
        if (existe) {
            throw new IllegalArgumentException("La especialidad ya existe");
        }
    }

    private void validarDescripcion(String descripcion) {
        if (descripcion != null && descripcion.length() > 200) {
            throw new IllegalArgumentException("La descripción no puede superar los 200 caracteres");
        }
    }
}
