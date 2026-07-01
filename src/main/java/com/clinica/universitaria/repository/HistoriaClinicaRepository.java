package com.clinica.universitaria.repository;

import com.clinica.universitaria.model.HistoriaClinica;
import com.clinica.universitaria.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HistoriaClinicaRepository extends JpaRepository<HistoriaClinica, Long> {
    Optional<HistoriaClinica> findByPaciente(Paciente paciente);
}
