package com.clinica.universitaria.service;

import com.clinica.universitaria.dto.PracticanteFormDTO;
import com.clinica.universitaria.model.Doctor;
import com.clinica.universitaria.model.Especialidad;
import com.clinica.universitaria.model.Practicante;
import com.clinica.universitaria.model.NombreRol;
import com.clinica.universitaria.model.Usuario;
import com.clinica.universitaria.repository.DoctorRepository;
import com.clinica.universitaria.repository.EspecialidadRepository;
import com.clinica.universitaria.repository.PracticanteRepository;
import com.clinica.universitaria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PracticanteService {

    private final PracticanteRepository practicanteRepository;
    private final UsuarioRepository usuarioRepository;
    private final EspecialidadRepository especialidadRepository;
    private final DoctorRepository doctorRepository;
    private final RolService rolService;
    private final PasswordEncoder passwordEncoder;

    public List<Practicante> listar(String q, Long supervisorId, Boolean estado) {
        return practicanteRepository.buscar(texto(q), supervisorId, estado);
    }

    public Practicante obtenerPorId(Long id) {
        return practicanteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Practicante no encontrado"));
    }

    public Practicante obtenerPorCorreo(String correo) {
        return practicanteRepository.findByUsuarioCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Practicante no encontrado"));
    }

    public long contarActivos() {
        return practicanteRepository.countByEstadoTrue();
    }

    @Transactional
    public Practicante crear(PracticanteFormDTO dto) {
        validarUnicidad(dto.getDni(), dto.getCorreo(), dto.getCodigoUniversitario(), null);
        Usuario usuario = Usuario.builder()
                .rol(rolService.obtenerPorNombre(NombreRol.PRACTICANTE))
                .dni(dto.getDni())
                .nombres(dto.getNombres())
                .apellidos(dto.getApellidos())
                .correo(dto.getCorreo())
                .passwordHash(passwordEncoder.encode(StringUtils.hasText(dto.getPassword()) ? dto.getPassword() : "123456"))
                .telefono(dto.getTelefono())
                .estado(dto.getEstado())
                .build();
        usuario = usuarioRepository.save(usuario);
        Especialidad especialidad = especialidadRepository.findById(dto.getEspecialidadId())
                .orElseThrow(() -> new IllegalArgumentException("Especialidad no encontrada"));
        Practicante practicante = Practicante.builder()
                .usuario(usuario)
                .especialidad(especialidad)
                .doctorSupervisor(resolverSupervisor(dto.getDoctorSupervisorId(), especialidad))
                .codigoUniversitario(dto.getCodigoUniversitario())
                .ciclo(dto.getCiclo())
                .estado(dto.getEstado())
                .build();
        return practicanteRepository.save(practicante);
    }

    @Transactional
    public Practicante actualizar(Long id, PracticanteFormDTO dto) {
        Practicante practicante = obtenerPorId(id);
        if (!practicante.getUsuario().getDni().equals(dto.getDni())) {
            throw new IllegalArgumentException("El DNI no puede modificarse una vez creado el practicante");
        }
        validarUnicidad(practicante.getUsuario().getDni(), dto.getCorreo(), dto.getCodigoUniversitario(), practicante.getUsuario().getId());
        Usuario usuario = practicante.getUsuario();
        usuario.setNombres(dto.getNombres());
        usuario.setApellidos(dto.getApellidos());
        usuario.setCorreo(dto.getCorreo());
        usuario.setTelefono(dto.getTelefono());
        usuario.setEstado(dto.getEstado());
        if (StringUtils.hasText(dto.getPassword())) {
            usuario.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
        usuarioRepository.save(usuario);
        Especialidad especialidad = especialidadRepository.findById(dto.getEspecialidadId())
                .orElseThrow(() -> new IllegalArgumentException("Especialidad no encontrada"));
        practicante.setEspecialidad(especialidad);
        practicante.setDoctorSupervisor(resolverSupervisor(dto.getDoctorSupervisorId(), especialidad));
        practicante.setCodigoUniversitario(dto.getCodigoUniversitario());
        practicante.setCiclo(dto.getCiclo());
        practicante.setEstado(dto.getEstado());
        return practicanteRepository.save(practicante);
    }

    @Transactional
    public void desactivar(Long id) {
        Practicante practicante = obtenerPorId(id);
        practicante.setEstado(false);
        practicanteRepository.save(practicante);
        Usuario usuario = practicante.getUsuario();
        usuario.setEstado(false);
        usuarioRepository.save(usuario);
    }

    private void validarUnicidad(String dni, String correo, String codigoUniversitario, Long usuarioExcluido) {
        List<Usuario> usuarios = usuarioRepository.findAll().stream()
                .filter(u -> usuarioExcluido == null || !u.getId().equals(usuarioExcluido))
                .toList();
        if (usuarios.stream().anyMatch(u -> u.getDni().equalsIgnoreCase(dni))) {
            throw new IllegalArgumentException("El DNI ya se encuentra registrado");
        }
        if (usuarios.stream().anyMatch(u -> u.getCorreo().equalsIgnoreCase(correo))) {
            throw new IllegalArgumentException("El correo ya se encuentra registrado");
        }
        boolean codigoDuplicado = practicanteRepository.findAll().stream()
                .filter(p -> usuarioExcluido == null || !p.getUsuario().getId().equals(usuarioExcluido))
                .anyMatch(p -> p.getCodigoUniversitario().equalsIgnoreCase(codigoUniversitario));
        if (codigoDuplicado) {
            throw new IllegalArgumentException("El código universitario ya se encuentra registrado");
        }
    }

    private Doctor resolverSupervisor(Long doctorSupervisorId, Especialidad especialidad) {
        if (doctorSupervisorId == null) {
            return null;
        }

        Doctor doctor = doctorRepository.findById(doctorSupervisorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor supervisor no encontrado"));

        if (!Boolean.TRUE.equals(doctor.getEstado())) {
            throw new IllegalArgumentException("El doctor supervisor seleccionado no está activo");
        }

        if (!doctor.getEspecialidad().getId().equals(especialidad.getId())) {
            throw new IllegalArgumentException("El doctor supervisor debe pertenecer a la especialidad seleccionada");
        }

        return doctor;
    }

    private String texto(String q) {
        return StringUtils.hasText(q) ? q.trim() : null;
    }
}
