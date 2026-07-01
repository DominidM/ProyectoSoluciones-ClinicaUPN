package com.clinica.universitaria.repository;

import com.clinica.universitaria.model.Consultorio;
import com.clinica.universitaria.model.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ConsultorioRepository extends JpaRepository<Consultorio, Long> {
    long countByEstadoTrue();

    List<Consultorio> findByEspecialidadAndEstadoTrueOrderByNombreAsc(Especialidad especialidad);

    @Query("""
            select c from Consultorio c
            where (:estado is null or c.estado = :estado)
            and (:especialidadId is null or c.especialidad.id = :especialidadId)
            and (
                :q is null or lower(c.nombre) like lower(concat('%', :q, '%'))
                or lower(c.numero) like lower(concat('%', :q, '%'))
            )
            order by c.nombre asc
            """)
    List<Consultorio> buscar(String q, Long especialidadId, Boolean estado);
}
