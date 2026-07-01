package com.clinica.universitaria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PacienteFormDTO {

    @NotBlank(message = "El DNI es obligatorio")
    private String dni;

    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Correo no válido")
    private String correo;

    @Pattern(regexp = "^$|.{6,}$", message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "El sexo es obligatorio")
    private String sexo;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @NotBlank(message = "El contacto de emergencia es obligatorio")
    private String contactoEmergencia;

    @NotBlank(message = "El teléfono de emergencia es obligatorio")
    private String telefonoEmergencia;

    @NotNull(message = "El estado es obligatorio")
    private Boolean estado;
}
