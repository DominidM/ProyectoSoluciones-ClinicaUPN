package com.clinica.universitaria.service;

import com.clinica.universitaria.model.NombreRol;
import com.clinica.universitaria.model.Rol;
import com.clinica.universitaria.repository.RolRepository;
import com.clinica.universitaria.repository.RolStoredProcedureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RolService {

    private final RolRepository rolRepository;
    private final RolStoredProcedureRepository rolStoredProcedureRepository;

    public List<Rol> listarActivos() {
        return rolStoredProcedureRepository.listarActivos();
    }

    public Rol obtenerPorNombre(NombreRol nombreRol) {
        return rolRepository.findByNombre(nombreRol)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + nombreRol));
    }
}
