USE clinica_universitaria;

DROP PROCEDURE IF EXISTS sp_rol_listar_activos;
DROP PROCEDURE IF EXISTS sp_usuario_contar_activos_por_rol;
DROP PROCEDURE IF EXISTS sp_usuario_desactivar;
DROP PROCEDURE IF EXISTS sp_usuario_actualizar;
DROP PROCEDURE IF EXISTS sp_usuario_crear;
DROP PROCEDURE IF EXISTS sp_usuario_obtener_por_correo;
DROP PROCEDURE IF EXISTS sp_usuario_obtener_por_id;
DROP PROCEDURE IF EXISTS sp_usuario_listar;

DELIMITER $$

CREATE PROCEDURE sp_rol_listar_activos()
BEGIN
    SELECT
        r.id_rol,
        r.nombre,
        r.descripcion,
        r.estado
    FROM roles r
    WHERE r.estado = 1
    ORDER BY r.nombre ASC;
END $$

CREATE PROCEDURE sp_usuario_listar(
    IN p_q VARCHAR(120),
    IN p_nombre_rol VARCHAR(50),
    IN p_estado TINYINT
)
BEGIN
    SET p_q = NULLIF(TRIM(p_q), '');
    SET p_nombre_rol = NULLIF(TRIM(p_nombre_rol), '');

    SELECT
        u.id_usuario,
        u.id_rol,
        r.nombre AS rol_nombre,
        r.descripcion AS rol_descripcion,
        r.estado AS rol_estado,
        u.dni,
        u.nombres,
        u.apellidos,
        CONCAT(u.nombres, ' ', u.apellidos) AS nombre_completo,
        u.correo,
        u.password_hash,
        u.telefono,
        u.estado,
        u.fecha_creacion
    FROM usuarios u
    INNER JOIN roles r ON r.id_rol = u.id_rol
    WHERE (p_nombre_rol IS NULL OR r.nombre = p_nombre_rol)
      AND (p_estado IS NULL OR u.estado = p_estado)
      AND (
            p_q IS NULL
            OR LOWER(u.nombres) LIKE LOWER(CONCAT('%', p_q, '%'))
            OR LOWER(u.apellidos) LIKE LOWER(CONCAT('%', p_q, '%'))
            OR LOWER(u.correo) LIKE LOWER(CONCAT('%', p_q, '%'))
            OR LOWER(u.dni) LIKE LOWER(CONCAT('%', p_q, '%'))
          )
    ORDER BY u.fecha_creacion DESC;
END $$

CREATE PROCEDURE sp_usuario_obtener_por_id(
    IN p_id_usuario BIGINT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM usuarios
        WHERE id_usuario = p_id_usuario
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Usuario no encontrado';
    END IF;

    SELECT
        u.id_usuario,
        u.id_rol,
        r.nombre AS rol_nombre,
        r.descripcion AS rol_descripcion,
        r.estado AS rol_estado,
        u.dni,
        u.nombres,
        u.apellidos,
        CONCAT(u.nombres, ' ', u.apellidos) AS nombre_completo,
        u.correo,
        u.password_hash,
        u.telefono,
        u.estado,
        u.fecha_creacion
    FROM usuarios u
    INNER JOIN roles r ON r.id_rol = u.id_rol
    WHERE u.id_usuario = p_id_usuario;
END $$

CREATE PROCEDURE sp_usuario_obtener_por_correo(
    IN p_correo VARCHAR(120)
)
BEGIN
    SET p_correo = NULLIF(TRIM(p_correo), '');

    IF p_correo IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'El correo es obligatorio';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM usuarios
        WHERE correo = p_correo
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Usuario no encontrado';
    END IF;

    SELECT
        u.id_usuario,
        u.id_rol,
        r.nombre AS rol_nombre,
        r.descripcion AS rol_descripcion,
        r.estado AS rol_estado,
        u.dni,
        u.nombres,
        u.apellidos,
        CONCAT(u.nombres, ' ', u.apellidos) AS nombre_completo,
        u.correo,
        u.password_hash,
        u.telefono,
        u.estado,
        u.fecha_creacion
    FROM usuarios u
    INNER JOIN roles r ON r.id_rol = u.id_rol
    WHERE u.correo = p_correo;
END $$

CREATE PROCEDURE sp_usuario_crear(
    IN p_nombre_rol VARCHAR(50),
    IN p_dni VARCHAR(15),
    IN p_nombres VARCHAR(100),
    IN p_apellidos VARCHAR(100),
    IN p_correo VARCHAR(120),
    IN p_password_hash VARCHAR(255),
    IN p_telefono VARCHAR(20),
    IN p_estado TINYINT
)
BEGIN
    DECLARE v_id_rol BIGINT;
    DECLARE v_id_usuario BIGINT;

    SET p_nombre_rol = NULLIF(TRIM(p_nombre_rol), '');
    SET p_dni = NULLIF(TRIM(p_dni), '');
    SET p_nombres = NULLIF(TRIM(p_nombres), '');
    SET p_apellidos = NULLIF(TRIM(p_apellidos), '');
    SET p_correo = NULLIF(TRIM(p_correo), '');
    SET p_password_hash = NULLIF(TRIM(p_password_hash), '');
    SET p_telefono = NULLIF(TRIM(p_telefono), '');

    IF p_nombre_rol IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'El rol es obligatorio';
    END IF;

    IF p_dni IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'El DNI es obligatorio';
    END IF;

    IF p_nombres IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Los nombres son obligatorios';
    END IF;

    IF p_apellidos IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Los apellidos son obligatorios';
    END IF;

    IF p_correo IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'El correo es obligatorio';
    END IF;

    IF p_password_hash IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'El password_hash es obligatorio';
    END IF;

    SELECT r.id_rol
      INTO v_id_rol
    FROM roles r
    WHERE r.nombre = p_nombre_rol
      AND r.estado = 1
    LIMIT 1;

    IF v_id_rol IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Rol no encontrado o inactivo';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM usuarios
        WHERE dni = p_dni
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'El DNI ya se encuentra registrado';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM usuarios
        WHERE correo = p_correo
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'El correo ya se encuentra registrado';
    END IF;

    INSERT INTO usuarios (
        id_rol,
        dni,
        nombres,
        apellidos,
        correo,
        password_hash,
        telefono,
        estado
    )
    VALUES (
        v_id_rol,
        p_dni,
        p_nombres,
        p_apellidos,
        p_correo,
        p_password_hash,
        p_telefono,
        COALESCE(p_estado, 1)
    );

    SET v_id_usuario = LAST_INSERT_ID();

    CALL sp_usuario_obtener_por_id(v_id_usuario);
END $$

CREATE PROCEDURE sp_usuario_actualizar(
    IN p_id_usuario BIGINT,
    IN p_nombre_rol VARCHAR(50),
    IN p_nombres VARCHAR(100),
    IN p_apellidos VARCHAR(100),
    IN p_correo VARCHAR(120),
    IN p_password_hash VARCHAR(255),
    IN p_telefono VARCHAR(20),
    IN p_estado TINYINT
)
BEGIN
    DECLARE v_id_rol BIGINT;

    SET p_nombre_rol = NULLIF(TRIM(p_nombre_rol), '');
    SET p_nombres = NULLIF(TRIM(p_nombres), '');
    SET p_apellidos = NULLIF(TRIM(p_apellidos), '');
    SET p_correo = NULLIF(TRIM(p_correo), '');
    SET p_password_hash = NULLIF(TRIM(p_password_hash), '');
    SET p_telefono = NULLIF(TRIM(p_telefono), '');

    IF NOT EXISTS (
        SELECT 1
        FROM usuarios
        WHERE id_usuario = p_id_usuario
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Usuario no encontrado';
    END IF;

    IF p_nombre_rol IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'El rol es obligatorio';
    END IF;

    IF p_nombres IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Los nombres son obligatorios';
    END IF;

    IF p_apellidos IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Los apellidos son obligatorios';
    END IF;

    IF p_correo IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'El correo es obligatorio';
    END IF;

    SELECT r.id_rol
      INTO v_id_rol
    FROM roles r
    WHERE r.nombre = p_nombre_rol
      AND r.estado = 1
    LIMIT 1;

    IF v_id_rol IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Rol no encontrado o inactivo';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM usuarios
        WHERE correo = p_correo
          AND id_usuario <> p_id_usuario
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'El correo ya se encuentra registrado';
    END IF;

    UPDATE usuarios
    SET id_rol = v_id_rol,
        nombres = p_nombres,
        apellidos = p_apellidos,
        correo = p_correo,
        telefono = p_telefono,
        estado = COALESCE(p_estado, estado),
        password_hash = COALESCE(p_password_hash, password_hash)
    WHERE id_usuario = p_id_usuario;

    CALL sp_usuario_obtener_por_id(p_id_usuario);
END $$

CREATE PROCEDURE sp_usuario_desactivar(
    IN p_id_usuario BIGINT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM usuarios
        WHERE id_usuario = p_id_usuario
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Usuario no encontrado';
    END IF;

    UPDATE usuarios
    SET estado = 0
    WHERE id_usuario = p_id_usuario;

    CALL sp_usuario_obtener_por_id(p_id_usuario);
END $$

CREATE PROCEDURE sp_usuario_contar_activos_por_rol(
    IN p_nombre_rol VARCHAR(50)
)
BEGIN
    SET p_nombre_rol = NULLIF(TRIM(p_nombre_rol), '');

    IF p_nombre_rol IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'El rol es obligatorio';
    END IF;

    SELECT COUNT(*) AS total_activos
    FROM usuarios u
    INNER JOIN roles r ON r.id_rol = u.id_rol
    WHERE r.nombre = p_nombre_rol
      AND u.estado = 1;
END $$

DELIMITER ;

-- Ejemplos de uso:
-- CALL sp_rol_listar_activos();
-- CALL sp_usuario_listar(NULL, NULL, NULL);
-- CALL sp_usuario_obtener_por_id(1);
-- CALL sp_usuario_obtener_por_correo('admin@upn.edu.pe');
-- CALL sp_usuario_crear('ADMIN', '12345678', 'Ana', 'Torres', 'ana@upn.edu.pe', '$2a$10$hash_bcrypt_aqui', '999888777', 1);
-- CALL sp_usuario_actualizar(1, 'ADMIN', 'Ana María', 'Torres', 'ana@upn.edu.pe', NULL, '999888777', 1);
-- CALL sp_usuario_desactivar(1);
-- CALL sp_usuario_contar_activos_por_rol('ADMIN');
