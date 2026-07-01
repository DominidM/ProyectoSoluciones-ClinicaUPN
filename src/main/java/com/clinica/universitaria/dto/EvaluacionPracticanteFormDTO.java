package com.clinica.universitaria.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class EvaluacionPracticanteFormDTO {

    @NotNull(message = "El practicante es obligatorio")
    private Long practicanteId;

    @NotNull(message = "El doctor supervisor es obligatorio")
    private Long doctorSupervisorId;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fechaEvaluacion;

    @NotNull(message = "El puntaje es obligatorio")
    @DecimalMin(value = "0.0", message = "El puntaje no puede ser menor a 0")
    @DecimalMax(value = "20.0", message = "El puntaje no puede ser mayor a 20")
    private BigDecimal puntaje;

    private String comentario;

    @NotNull(message = "El estado es obligatorio")
    private Boolean estado;
}
