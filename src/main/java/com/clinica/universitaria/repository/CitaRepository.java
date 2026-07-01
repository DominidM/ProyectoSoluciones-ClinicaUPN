package com.clinica.universitaria.repository;

import com.clinica.universitaria.model.Cita;
import com.clinica.universitaria.model.Doctor;
import com.clinica.universitaria.model.EstadoCita;
import com.clinica.universitaria.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByDoctorOrderByFechaCitaDescHoraInicioDesc(Doctor doctor);

    List<Cita> findByPacienteOrderByFechaCitaDescHoraInicioDesc(Paciente paciente);

    long countByFechaCita(LocalDate fechaCita);

    long countByEstado(EstadoCita estado);

    long countByEstadoAndFechaCita(EstadoCita estado, LocalDate fechaCita);

    boolean existsByDoctorIdAndFechaCitaAndHoraInicio(Long doctorId, LocalDate fechaCita, LocalTime horaInicio);

    boolean existsByConsultorioIdAndFechaCitaAndHoraInicio(Long consultorioId, LocalDate fechaCita, LocalTime horaInicio);

    List<Cita> findTop8ByFechaCitaGreaterThanEqualOrderByFechaCitaAscHoraInicioAsc(LocalDate fecha);

    @Query("""
            select c from Cita c
            where (:doctorId is null or c.doctor.id = :doctorId)
            and (:pacienteId is null or c.paciente.id = :pacienteId)
            and (:estado is null or c.estado = :estado)
            and (
                :q is null or lower(c.paciente.usuario.nombres) like lower(concat('%', :q, '%'))
                or lower(c.paciente.usuario.apellidos) like lower(concat('%', :q, '%'))
                or lower(c.doctor.usuario.nombres) like lower(concat('%', :q, '%'))
                or lower(c.especialidad.nombre) like lower(concat('%', :q, '%'))
            )
            order by c.fechaCita desc, c.horaInicio desc
            """)
    List<Cita> buscar(String q, Long doctorId, Long pacienteId, EstadoCita estado);
}
