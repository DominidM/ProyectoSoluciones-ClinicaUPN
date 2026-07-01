package com.clinica.universitaria.repository;

import com.clinica.universitaria.model.NombreRol;
import com.clinica.universitaria.model.Rol;
import com.clinica.universitaria.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UsuarioStoredProcedureRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<Usuario> listar(String q, NombreRol rol, Boolean estado) {
        try {
            return jdbcTemplate.query(
                    "CALL sp_usuario_listar(?, ?, ?)",
                    usuarioRowMapper(),
                    q,
                    rol == null ? null : rol.name(),
                    estado
            );
        } catch (DataAccessException ex) {
            throw traducirExcepcion("No se pudieron listar los usuarios", ex);
        }
    }

    public Optional<Usuario> obtenerPorId(Long id) {
        try {
            return jdbcTemplate.query(
                    "CALL sp_usuario_obtener_por_id(?)",
                    usuarioRowMapper(),
                    id
            ).stream().findFirst();
        } catch (DataAccessException ex) {
            throw traducirExcepcion("No se pudo obtener el usuario", ex);
        }
    }

    public Optional<Usuario> obtenerPorCorreo(String correo) {
        try {
            return jdbcTemplate.query(
                    "CALL sp_usuario_obtener_por_correo(?)",
                    usuarioRowMapper(),
                    correo
            ).stream().findFirst();
        } catch (DataAccessException ex) {
            throw traducirExcepcion("No se pudo obtener el usuario por correo", ex);
        }
    }

    public Usuario crear(NombreRol rol, String dni, String nombres, String apellidos, String correo,
                         String passwordHash, String telefono, Boolean estado) {
        try {
            return jdbcTemplate.execute(
                    (CallableStatementCreator) con -> {
                        CallableStatement cs = con.prepareCall("{call sp_usuario_crear(?, ?, ?, ?, ?, ?, ?, ?)}");
                        cs.setString(1, rol == null ? null : rol.name());
                        cs.setString(2, dni);
                        cs.setString(3, nombres);
                        cs.setString(4, apellidos);
                        cs.setString(5, correo);
                        cs.setString(6, passwordHash);
                        cs.setString(7, telefono);
                        if (estado == null) {
                            cs.setNull(8, java.sql.Types.TINYINT);
                        } else {
                            cs.setBoolean(8, estado);
                        }
                        return cs;
                    },
                    (CallableStatementCallback<Usuario>) cs ->
                            ejecutarLecturaDeUsuario(cs, "No se pudo crear el usuario")
            );
        } catch (DataAccessException ex) {
            throw traducirExcepcion("No se pudo crear el usuario", ex);
        }
    }

    public Usuario actualizar(Long id, NombreRol rol, String nombres, String apellidos, String correo,
                              String passwordHash, String telefono, Boolean estado) {
        try {
            return jdbcTemplate.execute(
                    (CallableStatementCreator) con -> {
                        CallableStatement cs = con.prepareCall("{call sp_usuario_actualizar(?, ?, ?, ?, ?, ?, ?, ?)}");
                        cs.setLong(1, id);
                        cs.setString(2, rol == null ? null : rol.name());
                        cs.setString(3, nombres);
                        cs.setString(4, apellidos);
                        cs.setString(5, correo);
                        cs.setString(6, passwordHash);
                        cs.setString(7, telefono);
                        if (estado == null) {
                            cs.setNull(8, java.sql.Types.TINYINT);
                        } else {
                            cs.setBoolean(8, estado);
                        }
                        return cs;
                    },
                    (CallableStatementCallback<Usuario>) cs ->
                            ejecutarLecturaDeUsuario(cs, "No se pudo actualizar el usuario")
            );
        } catch (DataAccessException ex) {
            throw traducirExcepcion("No se pudo actualizar el usuario", ex);
        }
    }

    public Usuario desactivar(Long id) {
        try {
            return jdbcTemplate.execute(
                    (CallableStatementCreator) con -> {
                        CallableStatement cs = con.prepareCall("{call sp_usuario_desactivar(?)}");
                        cs.setLong(1, id);
                        return cs;
                    },
                    (CallableStatementCallback<Usuario>) cs ->
                            ejecutarLecturaDeUsuario(cs, "No se pudo desactivar el usuario")
            );
        } catch (DataAccessException ex) {
            throw traducirExcepcion("No se pudo desactivar el usuario", ex);
        }
    }

    public long contarActivosPorRol(NombreRol rol) {
        try {
            List<Long> resultado = jdbcTemplate.query(
                    "CALL sp_usuario_contar_activos_por_rol(?)",
                    (rs, rowNum) -> rs.getLong("total_activos"),
                    rol == null ? null : rol.name()
            );
            return resultado.isEmpty() ? 0L : resultado.get(0);
        } catch (DataAccessException ex) {
            throw traducirExcepcion("No se pudo contar los usuarios activos por rol", ex);
        }
    }

    private RowMapper<Usuario> usuarioRowMapper() {
        return (rs, rowNum) -> mapUsuario(rs);
    }

    private Boolean leerBoolean(ResultSet rs, String columna) throws SQLException {
        Object valor = rs.getObject(columna);
        return valor == null ? null : rs.getBoolean(columna);
    }

    private Usuario mapUsuario(ResultSet rs) throws SQLException {
        Rol rol = Rol.builder()
                .id(rs.getLong("id_rol"))
                .nombre(NombreRol.valueOf(rs.getString("rol_nombre")))
                .descripcion(rs.getString("rol_descripcion"))
                .estado(leerBoolean(rs, "rol_estado"))
                .build();

        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");

        return Usuario.builder()
                .id(rs.getLong("id_usuario"))
                .rol(rol)
                .dni(rs.getString("dni"))
                .nombres(rs.getString("nombres"))
                .apellidos(rs.getString("apellidos"))
                .correo(rs.getString("correo"))
                .passwordHash(rs.getString("password_hash"))
                .telefono(rs.getString("telefono"))
                .estado(leerBoolean(rs, "estado"))
                .fechaCreacion(fechaCreacion == null ? null : fechaCreacion.toLocalDateTime())
                .build();
    }

    private Usuario ejecutarLecturaDeUsuario(CallableStatement cs, String mensajeSiNoHayResultado) throws SQLException {
        boolean hayResultados = cs.execute();
        while (!hayResultados && cs.getUpdateCount() != -1) {
            hayResultados = cs.getMoreResults();
        }

        if (!hayResultados) {
            throw new IllegalArgumentException(mensajeSiNoHayResultado);
        }

        try (ResultSet rs = cs.getResultSet()) {
            if (rs != null && rs.next()) {
                return mapUsuario(rs);
            }
        }

        throw new IllegalArgumentException(mensajeSiNoHayResultado);
    }

    private IllegalArgumentException traducirExcepcion(String mensajePorDefecto, DataAccessException ex) {
        Throwable causa = ex.getMostSpecificCause();
        String mensaje = causa != null && causa.getMessage() != null ? causa.getMessage() : mensajePorDefecto;
        return new IllegalArgumentException(mensaje, ex);
    }
}
