package com.clinica.universitaria.service;

import com.clinica.universitaria.model.HistoriaClinica;
import com.clinica.universitaria.model.Paciente;
import com.clinica.universitaria.repository.HistoriaClinicaRepository;
import com.clinica.universitaria.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistoriaClinicaService {

    private final HistoriaClinicaRepository historiaClinicaRepository;
    private final PacienteRepository pacienteRepository;

    public HistoriaClinica obtenerPorPaciente(Paciente paciente) {
        return historiaClinicaRepository.findByPaciente(paciente)
                .orElseGet(() -> crearInicial(paciente));
    }

    public HistoriaClinica obtenerPorPacienteId(Long pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));
        return obtenerPorPaciente(paciente);
    }

    @Transactional
    public HistoriaClinica crearInicial(Paciente paciente) {
        return historiaClinicaRepository.save(HistoriaClinica.builder()
                .paciente(paciente)
                .fechaApertura(LocalDate.now())
                .antecedentes("Sin antecedentes relevantes registrados.")
                .alergias("No refiere alergias conocidas.")
                .enfermedadesPrevias("Sin enfermedades previas reportadas.")
                .observacionesGenerales("Historia clínica inicial.")
                .estado(true)
                .build());
    }
}
