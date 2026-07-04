package com.clinica.universitaria.repository;

import com.clinica.universitaria.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    Optional<Pago> findByCitaId(Long citaId);

    Optional<Pago> findByPreferenceId(String preferenceId);

    List<Pago> findByCitaPacienteId(Long pacienteId);
}
