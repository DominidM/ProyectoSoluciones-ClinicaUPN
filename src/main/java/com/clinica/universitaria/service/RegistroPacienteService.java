package com.clinica.universitaria.service;

import com.clinica.universitaria.dto.RegistroPacienteDTO;
import com.clinica.universitaria.model.HistoriaClinica;
import com.clinica.universitaria.model.NombreRol;
import com.clinica.universitaria.model.Paciente;
import com.clinica.universitaria.model.Usuario;
import com.clinica.universitaria.repository.HistoriaClinicaRepository;
import com.clinica.universitaria.repository.PacienteRepository;
import com.clinica.universitaria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegistroPacienteService {

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final HistoriaClinicaRepository historiaClinicaRepository;
    private final RolService rolService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Paciente registrar(RegistroPacienteDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmarPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }
        validarUnicidad(dto.getDni(), dto.getCorreo());
        Usuario usuario = Usuario.builder()
                .rol(rolService.obtenerPorNombre(NombreRol.PACIENTE))
                .dni(dto.getDni())
                .nombres(dto.getNombres())
                .apellidos(dto.getApellidos())
                .correo(dto.getCorreo())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .telefono(dto.getTelefono())
                .estado(true)
                .build();
        usuario = usuarioRepository.save(usuario);

        Paciente paciente = Paciente.builder()
                .usuario(usuario)
                .fechaNacimiento(dto.getFechaNacimiento())
                .sexo(dto.getSexo())
                .direccion(dto.getDireccion())
                .contactoEmergencia(dto.getContactoEmergencia())
                .telefonoEmergencia(dto.getTelefonoEmergencia())
                .estado(true)
                .build();
        paciente = pacienteRepository.save(paciente);

        historiaClinicaRepository.save(HistoriaClinica.builder()
                .paciente(paciente)
                .fechaApertura(LocalDate.now())
                .antecedentes("Sin antecedentes relevantes registrados.")
                .alergias("No refiere alergias conocidas.")
                .enfermedadesPrevias("Sin enfermedades previas reportadas.")
                .observacionesGenerales("Historia clínica creada automáticamente con el registro público.")
                .estado(true)
                .build());

        return paciente;
    }

    private void validarUnicidad(String dni, String correo) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        if (usuarios.stream().anyMatch(u -> u.getDni().equalsIgnoreCase(dni))) {
            throw new IllegalArgumentException("El DNI ya se encuentra registrado");
        }
        if (usuarios.stream().anyMatch(u -> u.getCorreo().equalsIgnoreCase(correo))) {
            throw new IllegalArgumentException("El correo ya se encuentra registrado");
        }
    }
}
