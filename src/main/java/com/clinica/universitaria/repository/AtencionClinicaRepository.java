package com.clinica.universitaria.repository;

import com.clinica.universitaria.model.AtencionClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AtencionClinicaRepository extends JpaRepository<AtencionClinica, Long> {
    List<AtencionClinica> findTop8ByOrderByFechaAtencionDesc();

    @Query("""
            select a from AtencionClinica a
            where (:doctorId is null or a.doctor.id = :doctorId)
            and (:practicanteId is null or a.practicante.id = :practicanteId)
            order by a.fechaAtencion desc
            """)
    List<AtencionClinica> buscar(Long doctorId, Long practicanteId);
}
