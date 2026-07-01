package com.clinica.universitaria.dto;

import com.clinica.universitaria.model.DiaSemana;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class HorarioDoctorFormDTO {

    @NotNull(message = "El doctor es obligatorio")
    private Long doctorId;

    @NotNull(message = "El día es obligatorio")
    private DiaSemana diaSemana;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime horaFin;

    @NotNull(message = "El estado es obligatorio")
    private Boolean estado;
}
