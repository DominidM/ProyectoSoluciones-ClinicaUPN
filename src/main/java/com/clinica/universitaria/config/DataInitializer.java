package com.clinica.universitaria.config;

import com.clinica.universitaria.model.Especialidad;
import com.clinica.universitaria.model.NombreRol;
import com.clinica.universitaria.model.Rol;
import com.clinica.universitaria.repository.EspecialidadRepository;
import com.clinica.universitaria.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RolRepository rolRepository;
    private final EspecialidadRepository especialidadRepository;

    @Bean
    CommandLineRunner loadData() {
        return args -> {
            crearRoles();
            crearEspecialidades();
        };
    }

    private void crearRoles() {
        crearRol(NombreRol.ADMIN, "Administrador con acceso total al sistema.");
        crearRol(NombreRol.DOCTOR, "Doctor clínico con acceso a pacientes, citas y atenciones.");
        crearRol(NombreRol.PRACTICANTE, "Practicante con seguimiento académico y clínico.");
        crearRol(NombreRol.PACIENTE, "Paciente con acceso a su portal personal.");
    }

    private void crearRol(NombreRol nombre, String descripcion) {
        rolRepository.findByNombre(nombre)
                .orElseGet(() -> rolRepository.save(Rol.builder()
                        .nombre(nombre)
                        .descripcion(descripcion)
                        .estado(true)
                        .build()));
    }

    private void crearEspecialidades() {
        crearEspecialidad("Medicina General", "Atención integral preventiva y curativa.");
        crearEspecialidad("Obstetricia", "Cuidado prenatal y salud reproductiva.");
        crearEspecialidad("Nutrición", "Orientación nutricional personalizada.");
        crearEspecialidad("Psicología", "Apoyo en salud mental y bienestar.");
        crearEspecialidad("Rehabilitación", "Recuperación física post lesión.");
        crearEspecialidad("Fisioterapia", "Mejora de movilidad y reducción de dolor.");
    }

    private void crearEspecialidad(String nombre, String descripcion) {
        especialidadRepository.findByNombreIgnoreCase(nombre)
                .orElseGet(() -> especialidadRepository.save(Especialidad.builder()
                        .nombre(nombre)
                        .descripcion(descripcion)
                        .estado(true)
                        .build()));
    }
}
