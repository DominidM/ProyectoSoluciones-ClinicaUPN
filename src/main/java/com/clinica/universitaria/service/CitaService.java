package com.clinica.universitaria.service;

import com.clinica.universitaria.dto.CitaFormDTO;
import com.clinica.universitaria.model.*;
import com.clinica.universitaria.repository.CitaRepository;
import com.clinica.universitaria.repository.ConsultorioRepository;
import com.clinica.universitaria.repository.DoctorRepository;
import com.clinica.universitaria.repository.EspecialidadRepository;
import com.clinica.universitaria.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CitaService {

    private final CitaRepository citaRepository;
    private final PacienteRepository pacienteRepository;
    private final DoctorRepository doctorRepository;
    private final EspecialidadRepository especialidadRepository;
    private final ConsultorioRepository consultorioRepository;

    public List<Cita> listarAdmin(String q, EstadoCita estado) {
        return citaRepository.buscar(texto(q), null, null, estado);
    }

    public List<Cita> listarPorDoctor(Doctor doctor) {
        return citaRepository.findByDoctorOrderByFechaCitaDescHoraInicioDesc(doctor);
    }

    public List<Cita> listarPorPaciente(Paciente paciente) {
        return citaRepository.findByPacienteOrderByFechaCitaDescHoraInicioDesc(paciente);
    }

    public Cita obtenerPorId(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));
    }

    public long contarHoy() {
        return citaRepository.countByFechaCita(LocalDate.now());
    }

    public long contarPorEstado(EstadoCita estadoCita) {
        return citaRepository.countByEstado(estadoCita);
    }

    public List<Cita> proximasGlobales() {
        return citaRepository.findTop8ByFechaCitaGreaterThanEqualOrderByFechaCitaAscHoraInicioAsc(LocalDate.now());
    }

    public List<Cita> proximasPorDoctor(Doctor doctor) {
        return listarPorDoctor(doctor).stream()
                .filter(c -> !c.getFechaCita().isBefore(LocalDate.now()))
                .sorted((a, b) -> {
                    int byDate = a.getFechaCita().compareTo(b.getFechaCita());
                    return byDate != 0 ? byDate : a.getHoraInicio().compareTo(b.getHoraInicio());
                })
                .limit(8)
                .toList();
    }

    public List<Cita> citasHoyPorDoctor(Doctor doctor) {
        return listarPorDoctor(doctor).stream()
                .filter(c -> c.getFechaCita().equals(LocalDate.now()))
                .sorted((a, b) -> a.getHoraInicio().compareTo(b.getHoraInicio()))
                .toList();
    }

    @Transactional
    public Cita crearComoAdmin(CitaFormDTO dto) {
        return guardarCita(null, dto, true);
    }

    @Transactional
    public Cita solicitarComoPaciente(Paciente paciente, CitaFormDTO dto) {
        dto.setPacienteId(paciente.getId());
        dto.setEstado(EstadoCita.PENDIENTE);
        return guardarCita(null, dto, false);
    }

    @Transactional
    public void cancelar(Long id) {
        Cita cita = obtenerPorId(id);
        cita.setEstado(EstadoCita.CANCELADA);
        citaRepository.save(cita);
    }

    private Cita guardarCita(Long id, CitaFormDTO dto, boolean adminFlow) {

        if (dto.getHoraInicio() == null) {
            throw new IllegalArgumentException("La hora de inicio es obligatoria");
        }

        if (dto.getHoraFin() == null) {
            dto.setHoraFin(dto.getHoraInicio().plusMinutes(30));
        }

        if (!dto.getHoraFin().isAfter(dto.getHoraInicio())) {
            throw new IllegalArgumentException("La hora de fin debe ser mayor a la hora de inicio");
        }

        Cita cita = id == null ? new Cita() : obtenerPorId(id);

        Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));

        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor no encontrado"));

        Especialidad especialidad = especialidadRepository.findById(dto.getEspecialidadId())
                .orElseThrow(() -> new IllegalArgumentException("Especialidad no encontrada"));

        Consultorio consultorio = resolverConsultorio(dto, doctor);

        validarEstados(paciente, doctor, consultorio);
        validarRelacionEspecialidad(doctor, especialidad, consultorio);
        validarDisponibilidad(id, doctor.getId(), consultorio.getId(), dto.getFechaCita(), dto.getHoraInicio());

        cita.setPaciente(paciente);
        cita.setDoctor(doctor);
        cita.setEspecialidad(especialidad);
        cita.setConsultorio(consultorio);
        cita.setFechaCita(dto.getFechaCita());
        cita.setHoraInicio(dto.getHoraInicio());
        cita.setHoraFin(dto.getHoraFin());
        cita.setMotivo(dto.getMotivo());
        cita.setEstado(adminFlow && dto.getEstado() != null ? dto.getEstado() : EstadoCita.PENDIENTE);

        return citaRepository.save(cita);
    }

    private Consultorio resolverConsultorio(CitaFormDTO dto, Doctor doctor) {
        if (dto.getConsultorioId() != null) {
            return consultorioRepository.findById(dto.getConsultorioId())
                    .orElseThrow(() -> new IllegalArgumentException("Consultorio no encontrado"));
        }
        return consultorioRepository.findByEspecialidadAndEstadoTrueOrderByNombreAsc(doctor.getEspecialidad()).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No hay consultorios activos para la especialidad seleccionada"));
    }

    private void validarEstados(Paciente paciente, Doctor doctor, Consultorio consultorio) {
        if (!paciente.getEstado()) {
            throw new IllegalArgumentException("No se puede registrar una cita con paciente inactivo");
        }
        if (!doctor.getEstado()) {
            throw new IllegalArgumentException("No se puede registrar una cita con doctor inactivo");
        }
        if (!consultorio.getEstado()) {
            throw new IllegalArgumentException("No se puede registrar una cita con consultorio inactivo");
        }
    }

    private void validarRelacionEspecialidad(Doctor doctor, Especialidad especialidad, Consultorio consultorio) {
        if (!doctor.getEspecialidad().getId().equals(especialidad.getId())) {
            throw new IllegalArgumentException("El doctor no pertenece a la especialidad seleccionada");
        }
        if (!consultorio.getEspecialidad().getId().equals(especialidad.getId())) {
            throw new IllegalArgumentException("El consultorio no pertenece a la especialidad seleccionada");
        }
    }

    private void validarDisponibilidad(Long citaId, Long doctorId, Long consultorioId, LocalDate fecha, LocalTime horaInicio) {
        List<Cita> citas = citaRepository.findAll();
        boolean doctorOcupado = citas.stream()
                .filter(c -> citaId == null || !c.getId().equals(citaId))
                .filter(c -> !Set.of(EstadoCita.CANCELADA).contains(c.getEstado()))
                .anyMatch(c -> c.getDoctor().getId().equals(doctorId)
                        && c.getFechaCita().equals(fecha)
                        && c.getHoraInicio().equals(horaInicio));
        if (doctorOcupado) {
            throw new IllegalArgumentException("El doctor ya tiene una cita en ese horario");
        }
        boolean consultorioOcupado = citas.stream()
                .filter(c -> citaId == null || !c.getId().equals(citaId))
                .filter(c -> !Set.of(EstadoCita.CANCELADA).contains(c.getEstado()))
                .anyMatch(c -> c.getConsultorio().getId().equals(consultorioId)
                        && c.getFechaCita().equals(fecha)
                        && c.getHoraInicio().equals(horaInicio));
        if (consultorioOcupado) {
            throw new IllegalArgumentException("El consultorio ya tiene una cita en ese horario");
        }
    }

    private String texto(String q) {
        return StringUtils.hasText(q) ? q.trim() : null;
    }
}
