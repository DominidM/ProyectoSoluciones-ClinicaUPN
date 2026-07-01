package com.clinica.universitaria.repository;

import com.clinica.universitaria.model.Doctor;
import com.clinica.universitaria.model.Especialidad;
import com.clinica.universitaria.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUsuario(Usuario usuario);

    Optional<Doctor> findByUsuarioCorreo(String correo);

    long countByEstadoTrue();

    List<Doctor> findByEspecialidadAndEstadoTrueOrderByUsuarioNombresAsc(Especialidad especialidad);

    @Query("""
            select d from Doctor d
            where (:estado is null or d.estado = :estado)
            and (:especialidadId is null or d.especialidad.id = :especialidadId)
            and (
                :q is null or lower(d.usuario.nombres) like lower(concat('%', :q, '%'))
                or lower(d.usuario.apellidos) like lower(concat('%', :q, '%'))
                or lower(d.usuario.correo) like lower(concat('%', :q, '%'))
                or lower(d.usuario.dni) like lower(concat('%', :q, '%'))
                or lower(d.cmp) like lower(concat('%', :q, '%'))
            )
            order by d.usuario.nombres asc
            """)
    List<Doctor> buscar(String q, Long especialidadId, Boolean estado);
}
