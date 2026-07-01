package com.clinica.universitaria.service;

import com.clinica.universitaria.dto.PacienteFormDTO;
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
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistoriaClinicaRepository historiaClinicaRepository;
    private final RolService rolService;
    private final PasswordEncoder passwordEncoder;

    public List<Paciente> listar(String q, Boolean estado) {
        return pacienteRepository.buscar(texto(q), estado);
    }

    public Paciente obtenerPorId(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));
    }

    public Paciente obtenerPorCorreo(String correo) {
        return pacienteRepository.findByUsuarioCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));
    }

    public long contarActivos() {
        return pacienteRepository.countByEstadoTrue();
    }

    @Transactional
    public Paciente crear(PacienteFormDTO dto) {
        validarUnicidad(dto.getDni(), dto.getCorreo(), null);
        Usuario usuario = Usuario.builder()
                .rol(rolService.obtenerPorNombre(NombreRol.PACIENTE))
                .dni(dto.getDni())
                .nombres(dto.getNombres())
                .apellidos(dto.getApellidos())
                .correo(dto.getCorreo())
                .passwordHash(passwordEncoder.encode(StringUtils.hasText(dto.getPassword()) ? dto.getPassword() : "123456"))
                .telefono(dto.getTelefono())
                .estado(dto.getEstado())
                .build();
        usuario = usuarioRepository.save(usuario);
        Paciente paciente = Paciente.builder()
                .usuario(usuario)
                .fechaNacimiento(dto.getFechaNacimiento())
                .sexo(dto.getSexo())
                .direccion(dto.getDireccion())
                .contactoEmergencia(dto.getContactoEmergencia())
                .telefonoEmergencia(dto.getTelefonoEmergencia())
                .estado(dto.getEstado())
                .build();
        paciente = pacienteRepository.save(paciente);
        historiaClinicaRepository.save(HistoriaClinica.builder()
                .paciente(paciente)
                .fechaApertura(LocalDate.now())
                .antecedentes("Sin antecedentes relevantes registrados.")
                .alergias("No refiere alergias conocidas.")
                .enfermedadesPrevias("Sin enfermedades previas reportadas.")
                .observacionesGenerales("Historia clínica creada desde administración.")
                .estado(dto.getEstado())
                .build());
        return paciente;
    }

    @Transactional
    public Paciente actualizar(Long id, PacienteFormDTO dto) {
        Paciente paciente = obtenerPorId(id);
        if (!paciente.getUsuario().getDni().equals(dto.getDni())) {
            throw new IllegalArgumentException("El DNI no puede modificarse una vez creado el paciente");
        }
        validarUnicidad(paciente.getUsuario().getDni(), dto.getCorreo(), paciente.getUsuario().getId());
        Usuario usuario = paciente.getUsuario();
        usuario.setNombres(dto.getNombres());
        usuario.setApellidos(dto.getApellidos());
        usuario.setCorreo(dto.getCorreo());
        usuario.setTelefono(dto.getTelefono());
        usuario.setEstado(dto.getEstado());
        if (StringUtils.hasText(dto.getPassword())) {
            usuario.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
        usuarioRepository.save(usuario);
        paciente.setFechaNacimiento(dto.getFechaNacimiento());
        paciente.setSexo(dto.getSexo());
        paciente.setDireccion(dto.getDireccion());
        paciente.setContactoEmergencia(dto.getContactoEmergencia());
        paciente.setTelefonoEmergencia(dto.getTelefonoEmergencia());
        paciente.setEstado(dto.getEstado());
        paciente = pacienteRepository.save(paciente);
        historiaClinicaRepository.findByPaciente(paciente).ifPresent(historia -> {
            historia.setEstado(dto.getEstado());
            historiaClinicaRepository.save(historia);
        });
        return paciente;
    }

    @Transactional
    public void desactivar(Long id) {
        Paciente paciente = obtenerPorId(id);
        paciente.setEstado(false);
        pacienteRepository.save(paciente);
        Usuario usuario = paciente.getUsuario();
        usuario.setEstado(false);
        usuarioRepository.save(usuario);
        historiaClinicaRepository.findByPaciente(paciente).ifPresent(historia -> {
            historia.setEstado(false);
            historiaClinicaRepository.save(historia);
        });
    }

    public List<Paciente> obtenerPacientesUnicosPorCitas(List<com.clinica.universitaria.model.Cita> citas) {
        Set<Long> ids = new LinkedHashSet<>();
        return citas.stream()
                .map(com.clinica.universitaria.model.Cita::getPaciente)
                .filter(paciente -> ids.add(paciente.getId()))
                .toList();
    }

    private void validarUnicidad(String dni, String correo, Long usuarioExcluido) {
        List<Usuario> usuarios = usuarioRepository.findAll().stream()
                .filter(u -> usuarioExcluido == null || !u.getId().equals(usuarioExcluido))
                .toList();
        if (usuarios.stream().anyMatch(u -> u.getDni().equalsIgnoreCase(dni))) {
            throw new IllegalArgumentException("El DNI ya se encuentra registrado");
        }
        if (usuarios.stream().anyMatch(u -> u.getCorreo().equalsIgnoreCase(correo))) {
            throw new IllegalArgumentException("El correo ya se encuentra registrado");
        }
    }

    private String texto(String q) {
        return StringUtils.hasText(q) ? q.trim() : null;
    }
}
