package com.clinica.universitaria.repository;

import com.clinica.universitaria.model.NombreRol;
import com.clinica.universitaria.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(NombreRol nombre);
}
