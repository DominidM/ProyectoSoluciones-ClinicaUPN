package com.clinica.universitaria.repository;

import com.clinica.universitaria.model.EvaluacionPracticante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface EvaluacionPracticanteRepository extends JpaRepository<EvaluacionPracticante, Long> {
    List<EvaluacionPracticante> findTop8ByOrderByFechaEvaluacionDesc();

    @Query("""
            select e from EvaluacionPracticante e
            where (:doctorId is null or e.doctorSupervisor.id = :doctorId)
            and (:practicanteId is null or e.practicante.id = :practicanteId)
            and (:estado is null or e.estado = :estado)
            order by e.fechaEvaluacion desc
            """)
    List<EvaluacionPracticante> buscar(Long doctorId, Long practicanteId, Boolean estado);

    @Query("select coalesce(avg(e.puntaje), 0) from EvaluacionPracticante e where e.practicante.id = :practicanteId and e.estado = true")
    BigDecimal promedioPorPracticante(Long practicanteId);
}
