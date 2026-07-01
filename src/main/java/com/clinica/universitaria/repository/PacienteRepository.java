package com.clinica.universitaria.repository;

import com.clinica.universitaria.model.Paciente;
import com.clinica.universitaria.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    Optional<Paciente> findByUsuario(Usuario usuario);

    Optional<Paciente> findByUsuarioCorreo(String correo);

    long countByEstadoTrue();

    @Query("""
            select p from Paciente p
            where (:estado is null or p.estado = :estado)
            and (
                :q is null or lower(p.usuario.nombres) like lower(concat('%', :q, '%'))
                or lower(p.usuario.apellidos) like lower(concat('%', :q, '%'))
                or lower(p.usuario.correo) like lower(concat('%', :q, '%'))
                or lower(p.usuario.dni) like lower(concat('%', :q, '%'))
            )
            order by p.usuario.nombres asc
            """)
    List<Paciente> buscar(String q, Boolean estado);
}
