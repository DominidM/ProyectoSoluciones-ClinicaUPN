DROP DATABASE IF EXISTS clinica_universitaria;

CREATE DATABASE clinica_universitaria
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE clinica_universitaria;

CREATE TABLE roles (
    id_rol BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(150),
    estado TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE usuarios (
    id_usuario BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_rol BIGINT NOT NULL,
    dni VARCHAR(15) NOT NULL UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    correo VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    telefono VARCHAR(20),
    estado TINYINT(1) NOT NULL DEFAULT 1,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuarios_roles
        FOREIGN KEY (id_rol)
        REFERENCES roles(id_rol)
);

CREATE TABLE especialidades (
    id_especialidad BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(200),
    estado TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE pacientes (
    id_paciente BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL UNIQUE,
    fecha_nacimiento DATE,
    sexo VARCHAR(20),
    direccion VARCHAR(200),
    contacto_emergencia VARCHAR(100),
    telefono_emergencia VARCHAR(20),
    estado TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_pacientes_usuarios
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario)
);

CREATE TABLE doctores (
    id_doctor BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL UNIQUE,
    id_especialidad BIGINT NOT NULL,
    cmp VARCHAR(30),
    experiencia VARCHAR(100),
    estado TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_doctores_usuarios
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario),
    CONSTRAINT fk_doctores_especialidades
        FOREIGN KEY (id_especialidad)
        REFERENCES especialidades(id_especialidad)
);

CREATE TABLE practicantes (
    id_practicante BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL UNIQUE,
    id_especialidad BIGINT NOT NULL,
    id_doctor_supervisor BIGINT NULL,
    codigo_universitario VARCHAR(30) NOT NULL UNIQUE,
    ciclo VARCHAR(20),
    estado TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_practicantes_usuarios
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario),
    CONSTRAINT fk_practicantes_especialidades
        FOREIGN KEY (id_especialidad)
        REFERENCES especialidades(id_especialidad),
    CONSTRAINT fk_practicantes_doctores
        FOREIGN KEY (id_doctor_supervisor)
        REFERENCES doctores(id_doctor)
        ON DELETE SET NULL
);

CREATE TABLE consultorios (
    id_consultorio BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_especialidad BIGINT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    numero VARCHAR(20) NOT NULL,
    piso VARCHAR(20),
    descripcion VARCHAR(200),
    estado TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_consultorios_especialidades
        FOREIGN KEY (id_especialidad)
        REFERENCES especialidades(id_especialidad)
);

CREATE TABLE horarios_doctor (
    id_horario BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_doctor BIGINT NOT NULL,
    dia_semana ENUM(
        'LUNES',
        'MARTES',
        'MIERCOLES',
        'JUEVES',
        'VIERNES',
        'SABADO',
        'DOMINGO'
    ) NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    estado TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_horarios_doctores
        FOREIGN KEY (id_doctor)
        REFERENCES doctores(id_doctor),
    CONSTRAINT chk_horario_valido
        CHECK (hora_inicio < hora_fin)
);

CREATE TABLE citas (
    id_cita BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_paciente BIGINT NOT NULL,
    id_doctor BIGINT NOT NULL,
    id_especialidad BIGINT NOT NULL,
    id_consultorio BIGINT NOT NULL,
    fecha_cita DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    motivo VARCHAR(250),
    estado ENUM(
        'PENDIENTE',
        'CONFIRMADA',
        'ATENDIDA',
        'CANCELADA',
        'REPROGRAMADA'
    ) NOT NULL DEFAULT 'PENDIENTE',
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_citas_pacientes
        FOREIGN KEY (id_paciente)
        REFERENCES pacientes(id_paciente),
    CONSTRAINT fk_citas_doctores
        FOREIGN KEY (id_doctor)
        REFERENCES doctores(id_doctor),
    CONSTRAINT fk_citas_especialidades
        FOREIGN KEY (id_especialidad)
        REFERENCES especialidades(id_especialidad),
    CONSTRAINT fk_citas_consultorios
        FOREIGN KEY (id_consultorio)
        REFERENCES consultorios(id_consultorio),
    CONSTRAINT chk_cita_horario_valido
        CHECK (hora_inicio < hora_fin)
);

CREATE UNIQUE INDEX uk_cita_doctor_fecha_hora
    ON citas(id_doctor, fecha_cita, hora_inicio);

CREATE UNIQUE INDEX uk_cita_consultorio_fecha_hora
    ON citas(id_consultorio, fecha_cita, hora_inicio);

CREATE TABLE historias_clinicas (
    id_historia BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_paciente BIGINT NOT NULL UNIQUE,
    fecha_apertura DATE NOT NULL,
    antecedentes TEXT,
    alergias TEXT,
    enfermedades_previas TEXT,
    observaciones_generales TEXT,
    estado TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_historias_pacientes
        FOREIGN KEY (id_paciente)
        REFERENCES pacientes(id_paciente)
);

CREATE TABLE atenciones_clinicas (
    id_atencion BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_historia BIGINT NOT NULL,
    id_cita BIGINT NOT NULL UNIQUE,
    id_doctor BIGINT NOT NULL,
    id_practicante BIGINT NULL,
    fecha_atencion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motivo_consulta TEXT,
    diagnostico TEXT,
    tratamiento TEXT,
    observaciones TEXT,
    CONSTRAINT fk_atenciones_historias
        FOREIGN KEY (id_historia)
        REFERENCES historias_clinicas(id_historia),
    CONSTRAINT fk_atenciones_citas
        FOREIGN KEY (id_cita)
        REFERENCES citas(id_cita),
    CONSTRAINT fk_atenciones_doctores
        FOREIGN KEY (id_doctor)
        REFERENCES doctores(id_doctor),
    CONSTRAINT fk_atenciones_practicantes
        FOREIGN KEY (id_practicante)
        REFERENCES practicantes(id_practicante)
        ON DELETE SET NULL
);

CREATE TABLE evaluaciones_practicantes (
    id_evaluacion BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_practicante BIGINT NOT NULL,
    id_doctor_supervisor BIGINT NOT NULL,
    fecha_evaluacion DATE NOT NULL,
    puntaje DECIMAL(5,2) NOT NULL,
    comentario TEXT,
    estado TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_evaluaciones_practicantes
        FOREIGN KEY (id_practicante)
        REFERENCES practicantes(id_practicante),
    CONSTRAINT fk_evaluaciones_doctores
        FOREIGN KEY (id_doctor_supervisor)
        REFERENCES doctores(id_doctor),
    CONSTRAINT chk_puntaje_valido
        CHECK (puntaje >= 0 AND puntaje <= 20)
);
