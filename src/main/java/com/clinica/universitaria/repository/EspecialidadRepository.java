package com.clinica.universitaria.repository;

import com.clinica.universitaria.model.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {
    Optional<Especialidad> findByNombreIgnoreCase(String nombre);

    List<Especialidad> findByEstadoTrueOrderByNombreAsc();
}
