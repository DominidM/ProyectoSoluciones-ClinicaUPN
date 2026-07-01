package com.clinica.universitaria.repository;

import com.clinica.universitaria.model.NombreRol;
import com.clinica.universitaria.model.Rol;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RolStoredProcedureRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<Rol> listarActivos() {
        try {
            return jdbcTemplate.query("CALL sp_rol_listar_activos()", rolRowMapper());
        } catch (DataAccessException ex) {
            throw traducirExcepcion("No se pudieron listar los roles activos", ex);
        }
    }

    private RowMapper<Rol> rolRowMapper() {
        return (rs, rowNum) -> Rol.builder()
                .id(rs.getLong("id_rol"))
                .nombre(NombreRol.valueOf(rs.getString("nombre")))
                .descripcion(rs.getString("descripcion"))
                .estado(leerBoolean(rs, "estado"))
                .build();
    }

    private Boolean leerBoolean(ResultSet rs, String columna) throws SQLException {
        Object valor = rs.getObject(columna);
        return valor == null ? null : rs.getBoolean(columna);
    }

    private IllegalArgumentException traducirExcepcion(String mensajePorDefecto, DataAccessException ex) {
        Throwable causa = ex.getMostSpecificCause();
        String mensaje = causa != null && causa.getMessage() != null ? causa.getMessage() : mensajePorDefecto;
        return new IllegalArgumentException(mensaje, ex);
    }
}
