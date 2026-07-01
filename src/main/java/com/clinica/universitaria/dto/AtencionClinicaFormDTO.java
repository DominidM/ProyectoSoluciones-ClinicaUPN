package com.clinica.universitaria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AtencionClinicaFormDTO {

    @NotNull(message = "La cita es obligatoria")
    private Long citaId;

    private Long practicanteId;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivoConsulta;

    @NotBlank(message = "El diagnóstico es obligatorio")
    private String diagnostico;

    @NotBlank(message = "El tratamiento es obligatorio")
    private String tratamiento;

    private String observaciones;
}
