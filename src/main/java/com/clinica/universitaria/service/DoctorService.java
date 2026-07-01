package com.clinica.universitaria.service;

import com.clinica.universitaria.dto.DoctorFormDTO;
import com.clinica.universitaria.model.Doctor;
import com.clinica.universitaria.model.NombreRol;
import com.clinica.universitaria.model.Usuario;
import com.clinica.universitaria.repository.DoctorRepository;
import com.clinica.universitaria.repository.EspecialidadRepository;
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
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UsuarioRepository usuarioRepository;
    private final EspecialidadRepository especialidadRepository;
    private final RolService rolService;
    private final PasswordEncoder passwordEncoder;

    public List<Doctor> listar(String q, Long especialidadId, Boolean estado) {
        return doctorRepository.buscar(texto(q), especialidadId, estado);
    }

    public Doctor obtenerPorId(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor no encontrado"));
    }

    public Doctor obtenerPorCorreo(String correo) {
        return doctorRepository.findByUsuarioCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Doctor no encontrado"));
    }

    public long contarActivos() {
        return doctorRepository.countByEstadoTrue();
    }

    @Transactional
    public Doctor crear(DoctorFormDTO dto) {
        validarUnicidad(dto.getDni(), dto.getCorreo(), null);
        Usuario usuario = Usuario.builder()
                .rol(rolService.obtenerPorNombre(NombreRol.DOCTOR))
                .dni(dto.getDni())
                .nombres(dto.getNombres())
                .apellidos(dto.getApellidos())
                .correo(dto.getCorreo())
                .passwordHash(passwordEncoder.encode(StringUtils.hasText(dto.getPassword()) ? dto.getPassword() : "123456"))
                .telefono(dto.getTelefono())
                .estado(dto.getEstado())
                .build();
        usuario = usuarioRepository.save(usuario);
        Doctor doctor = Doctor.builder()
                .usuario(usuario)
                .especialidad(especialidadRepository.findById(dto.getEspecialidadId())
                        .orElseThrow(() -> new IllegalArgumentException("Especialidad no encontrada")))
                .cmp(dto.getCmp())
                .experiencia(dto.getExperiencia())
                .estado(dto.getEstado())
                .build();
        return doctorRepository.save(doctor);
    }

    @Transactional
    public Doctor actualizar(Long id, DoctorFormDTO dto) {
        Doctor doctor = obtenerPorId(id);
        if (!doctor.getUsuario().getDni().equals(dto.getDni())) {
            throw new IllegalArgumentException("El DNI no puede modificarse una vez creado el doctor");
        }
        validarUnicidad(doctor.getUsuario().getDni(), dto.getCorreo(), doctor.getUsuario().getId());
        Usuario usuario = doctor.getUsuario();
        usuario.setNombres(dto.getNombres());
        usuario.setApellidos(dto.getApellidos());
        usuario.setCorreo(dto.getCorreo());
        usuario.setTelefono(dto.getTelefono());
        usuario.setEstado(dto.getEstado());
        if (StringUtils.hasText(dto.getPassword())) {
            usuario.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
        usuarioRepository.save(usuario);
        doctor.setEspecialidad(especialidadRepository.findById(dto.getEspecialidadId())
                .orElseThrow(() -> new IllegalArgumentException("Especialidad no encontrada")));
        doctor.setCmp(dto.getCmp());
        doctor.setExperiencia(dto.getExperiencia());
        doctor.setEstado(dto.getEstado());
        return doctorRepository.save(doctor);
    }

    @Transactional
    public void desactivar(Long id) {
        Doctor doctor = obtenerPorId(id);
        doctor.setEstado(false);
        doctorRepository.save(doctor);
        Usuario usuario = doctor.getUsuario();
        usuario.setEstado(false);
        usuarioRepository.save(usuario);
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
