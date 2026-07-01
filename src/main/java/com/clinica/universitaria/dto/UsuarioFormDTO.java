package com.clinica.universitaria.dto;

import com.clinica.universitaria.model.NombreRol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioFormDTO {

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

    private String telefono;

    @NotNull(message = "El rol es obligatorio")
    private NombreRol rol;

    @NotNull(message = "El estado es obligatorio")
    private Boolean estado;
}
