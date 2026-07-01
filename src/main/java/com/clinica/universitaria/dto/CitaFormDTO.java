package com.clinica.universitaria.dto;

import com.clinica.universitaria.model.EstadoCita;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class CitaFormDTO {

    private Long pacienteId;

    @NotNull(message = "El doctor es obligatorio")
    private Long doctorId;

    @NotNull(message = "La especialidad es obligatoria")
    private Long especialidadId;

    private Long consultorioId;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fechaCita;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    private LocalTime horaFin;

    private String motivo;

    private EstadoCita estado = EstadoCita.PENDIENTE;
}
