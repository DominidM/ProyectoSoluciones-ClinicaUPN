package com.clinica.universitaria.repository;

import com.clinica.universitaria.model.Doctor;
import com.clinica.universitaria.model.Practicante;
import com.clinica.universitaria.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PracticanteRepository extends JpaRepository<Practicante, Long> {
    Optional<Practicante> findByUsuario(Usuario usuario);

    Optional<Practicante> findByUsuarioCorreo(String correo);

    boolean existsByCodigoUniversitario(String codigoUniversitario);

    long countByEstadoTrue();

    List<Practicante> findByDoctorSupervisorAndEstadoTrueOrderByUsuarioNombresAsc(Doctor doctorSupervisor);

    @Query("""
            select p from Practicante p
            where (:estado is null or p.estado = :estado)
            and (:supervisorId is null or p.doctorSupervisor.id = :supervisorId)
            and (
                :q is null or lower(p.usuario.nombres) like lower(concat('%', :q, '%'))
                or lower(p.usuario.apellidos) like lower(concat('%', :q, '%'))
                or lower(p.usuario.correo) like lower(concat('%', :q, '%'))
                or lower(p.codigoUniversitario) like lower(concat('%', :q, '%'))
            )
            order by p.usuario.nombres asc
            """)
    List<Practicante> buscar(String q, Long supervisorId, Boolean estado);
}
