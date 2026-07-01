package com.clinica.universitaria.repository;

import com.clinica.universitaria.model.NombreRol;
import com.clinica.universitaria.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    boolean existsByDni(String dni);

    long countByRolNombreAndEstadoTrue(NombreRol nombreRol);

    List<Usuario> findByEstadoTrueOrderByFechaCreacionDesc();

    @Query("""
            select u from Usuario u
            where (:rol is null or u.rol.nombre = :rol)
            and (:estado is null or u.estado = :estado)
            and (
                :q is null or lower(u.nombres) like lower(concat('%', :q, '%'))
                or lower(u.apellidos) like lower(concat('%', :q, '%'))
                or lower(u.correo) like lower(concat('%', :q, '%'))
                or lower(u.dni) like lower(concat('%', :q, '%'))
            )
            order by u.fechaCreacion desc
            """)
    List<Usuario> buscar(String q, NombreRol rol, Boolean estado);
}
