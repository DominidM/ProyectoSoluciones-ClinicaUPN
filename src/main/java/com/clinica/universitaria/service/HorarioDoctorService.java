package com.clinica.universitaria.service;

import com.clinica.universitaria.dto.HorarioDoctorFormDTO;
import com.clinica.universitaria.model.HorarioDoctor;
import com.clinica.universitaria.repository.DoctorRepository;
import com.clinica.universitaria.repository.HorarioDoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HorarioDoctorService {

    private final HorarioDoctorRepository horarioDoctorRepository;
    private final DoctorRepository doctorRepository;

    public List<HorarioDoctor> listar(Long doctorId, Boolean estado) {
        return horarioDoctorRepository.buscar(doctorId, estado);
    }

    public HorarioDoctor obtenerPorId(Long id) {
        return horarioDoctorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado"));
    }

    @Transactional
    public HorarioDoctor crear(HorarioDoctorFormDTO dto) {
        validarHoras(dto);
        return horarioDoctorRepository.save(HorarioDoctor.builder()
                .doctor(doctorRepository.findById(dto.getDoctorId())
                        .orElseThrow(() -> new IllegalArgumentException("Doctor no encontrado")))
                .diaSemana(dto.getDiaSemana())
                .horaInicio(dto.getHoraInicio())
                .horaFin(dto.getHoraFin())
                .estado(dto.getEstado())
                .build());
    }

    @Transactional
    public HorarioDoctor actualizar(Long id, HorarioDoctorFormDTO dto) {
        validarHoras(dto);
        HorarioDoctor horarioDoctor = obtenerPorId(id);
        horarioDoctor.setDoctor(doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor no encontrado")));
        horarioDoctor.setDiaSemana(dto.getDiaSemana());
        horarioDoctor.setHoraInicio(dto.getHoraInicio());
        horarioDoctor.setHoraFin(dto.getHoraFin());
        horarioDoctor.setEstado(dto.getEstado());
        return horarioDoctorRepository.save(horarioDoctor);
    }

    @Transactional
    public void eliminar(Long id) {
        HorarioDoctor horarioDoctor = obtenerPorId(id);
        horarioDoctor.setEstado(false);
        horarioDoctorRepository.save(horarioDoctor);
    }

    private void validarHoras(HorarioDoctorFormDTO dto) {
        if (!dto.getHoraFin().isAfter(dto.getHoraInicio())) {
            throw new IllegalArgumentException("La hora de fin debe ser mayor a la hora de inicio");
        }
    }
}
