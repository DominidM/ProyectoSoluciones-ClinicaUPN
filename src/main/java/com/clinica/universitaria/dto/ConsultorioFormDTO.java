package com.clinica.universitaria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsultorioFormDTO {

    @NotNull(message = "La especialidad es obligatoria")
    private Long especialidadId;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El número es obligatorio")
    private String numero;

    @NotBlank(message = "El piso es obligatorio")
    private String piso;

    private String descripcion;

    @NotNull(message = "El estado es obligatorio")
    private Boolean estado;
}
