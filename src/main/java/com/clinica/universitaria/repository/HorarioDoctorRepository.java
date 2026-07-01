package com.clinica.universitaria.repository;

import com.clinica.universitaria.model.Doctor;
import com.clinica.universitaria.model.HorarioDoctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HorarioDoctorRepository extends JpaRepository<HorarioDoctor, Long> {
    List<HorarioDoctor> findByDoctorAndEstadoTrueOrderByDiaSemanaAscHoraInicioAsc(Doctor doctor);

    @Query("""
            select h from HorarioDoctor h
            where (:doctorId is null or h.doctor.id = :doctorId)
            and (:estado is null or h.estado = :estado)
            order by h.doctor.usuario.nombres asc, h.diaSemana asc, h.horaInicio asc
            """)
    List<HorarioDoctor> buscar(Long doctorId, Boolean estado);
}
