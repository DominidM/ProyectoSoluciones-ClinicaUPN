package com.clinica.universitaria.service;

import com.clinica.universitaria.dto.UsuarioFormDTO;
import com.clinica.universitaria.model.*;
import com.clinica.universitaria.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioStoredProcedureRepository usuarioStoredProcedureRepository;
    private final PacienteRepository pacienteRepository;
    private final DoctorRepository doctorRepository;
    private final PracticanteRepository practicanteRepository;
    private final EspecialidadRepository especialidadRepository;
    private final HistoriaClinicaRepository historiaClinicaRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Usuario> listar(String q, NombreRol rol, Boolean estado) {
        return usuarioStoredProcedureRepository.listar(q, rol, estado);
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioStoredProcedureRepository.obtenerPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    public Usuario obtenerPorCorreo(String correo) {
        return usuarioStoredProcedureRepository.obtenerPorCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    public long contarPorRol(NombreRol nombreRol) {
        return usuarioStoredProcedureRepository.contarActivosPorRol(nombreRol);
    }

    @Transactional
    public Usuario crearDesdeFormulario(UsuarioFormDTO dto) {
        String passwordHash = passwordEncoder.encode(StringUtils.hasText(dto.getPassword()) ? dto.getPassword() : "123456");
        Usuario usuario = usuarioStoredProcedureRepository.crear(
                dto.getRol(),
                dto.getDni(),
                dto.getNombres(),
                dto.getApellidos(),
                dto.getCorreo(),
                passwordHash,
                dto.getTelefono(),
                dto.getEstado()
        );
        sincronizarPerfiles(usuarioRepository.getReferenceById(usuario.getId()), dto.getRol(), dto.getEstado());
        return usuario;
    }

    @Transactional
    public Usuario actualizar(Long id, UsuarioFormDTO dto) {
        Usuario usuario = obtenerPorId(id);
        if (!usuario.getDni().equals(dto.getDni())) {
            throw new IllegalArgumentException("El DNI no puede modificarse una vez creado el usuario");
        }
        String passwordHash = StringUtils.hasText(dto.getPassword())
                ? passwordEncoder.encode(dto.getPassword())
                : null;
        Usuario usuarioActualizado = usuarioStoredProcedureRepository.actualizar(
                id,
                dto.getRol(),
                dto.getNombres(),
                dto.getApellidos(),
                dto.getCorreo(),
                passwordHash,
                dto.getTelefono(),
                dto.getEstado()
        );
        sincronizarPerfiles(usuarioRepository.getReferenceById(usuarioActualizado.getId()),
                usuarioActualizado.getRol().getNombre(), dto.getEstado());
        return usuarioActualizado;
    }

    @Transactional
    public void desactivar(Long id) {
        Usuario usuario = usuarioStoredProcedureRepository.desactivar(id);
        sincronizarPerfiles(usuarioRepository.getReferenceById(usuario.getId()), usuario.getRol().getNombre(), false);
    }

    private void sincronizarPerfiles(Usuario usuario, NombreRol nombreRol, Boolean estado) {
        if (nombreRol != NombreRol.PACIENTE) {
            pacienteRepository.findByUsuario(usuario).ifPresent(p -> {
                p.setEstado(false);
                pacienteRepository.save(p);
                historiaClinicaRepository.findByPaciente(p).ifPresent(h -> {
                    h.setEstado(false);
                    historiaClinicaRepository.save(h);
                });
            });
        } else {
            Paciente paciente = pacienteRepository.findByUsuario(usuario).orElseGet(() -> Paciente.builder()
                    .usuario(usuario)
                    .fechaNacimiento(LocalDate.now().minusYears(18))
                    .sexo("No especificado")
                    .direccion("Pendiente de completar")
                    .contactoEmergencia("Pendiente")
                    .telefonoEmergencia("Pendiente")
                    .estado(estado)
                    .build());
            paciente.setEstado(estado);
            Paciente pacienteGuardado = pacienteRepository.save(paciente);
            HistoriaClinica historiaClinica = historiaClinicaRepository.findByPaciente(pacienteGuardado).orElseGet(() -> HistoriaClinica.builder()
                    .paciente(pacienteGuardado)
                    .fechaApertura(LocalDate.now())
                    .antecedentes("Sin antecedentes relevantes registrados.")
                    .alergias("No refiere alergias conocidas.")
                    .enfermedadesPrevias("Sin enfermedades previas reportadas.")
                    .observacionesGenerales("Historia clínica creada desde gestión de usuarios.")
                    .estado(estado)
                    .build());
            historiaClinica.setEstado(estado);
            historiaClinicaRepository.save(historiaClinica);
        }

        if (nombreRol != NombreRol.DOCTOR) {
            doctorRepository.findByUsuario(usuario).ifPresent(d -> {
                d.setEstado(false);
                doctorRepository.save(d);
            });
        } else {
            Doctor doctor = doctorRepository.findByUsuario(usuario).orElseGet(() -> Doctor.builder()
                    .usuario(usuario)
                    .especialidad(obtenerEspecialidadPorDefecto())
                    .cmp("CMP-" + String.format("%05d", usuario.getId()))
                    .experiencia("Pendiente de completar")
                    .estado(estado)
                    .build());
            doctor.setEstado(estado);
            doctorRepository.save(doctor);
        }

        if (nombreRol != NombreRol.PRACTICANTE) {
            practicanteRepository.findByUsuario(usuario).ifPresent(p -> {
                p.setEstado(false);
                practicanteRepository.save(p);
            });
        } else {
            Practicante practicante = practicanteRepository.findByUsuario(usuario).orElseGet(() -> Practicante.builder()
                    .usuario(usuario)
                    .especialidad(obtenerEspecialidadPorDefecto())
                    .codigoUniversitario("UPN-" + String.format("%04d", usuario.getId()))
                    .ciclo("Por definir")
                    .estado(estado)
                    .build());
            practicante.setEstado(estado);
            practicanteRepository.save(practicante);
        }
    }

    private Especialidad obtenerEspecialidadPorDefecto() {
        return especialidadRepository.findByEstadoTrueOrderByNombreAsc().stream()
                .min(Comparator.comparing(Especialidad::getNombre))
                .orElseThrow(() -> new IllegalArgumentException("No hay especialidades registradas"));
    }
}
