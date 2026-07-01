package com.clinica.universitaria.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "practicantes")
public class Practicante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_practicante")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_especialidad", nullable = false)
    private Especialidad especialidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_doctor_supervisor")
    private Doctor doctorSupervisor;

    @Column(name = "codigo_universitario", nullable = false, unique = true, length = 30)
    private String codigoUniversitario;

    @Column(length = 20)
    private String ciclo;

    @Column(nullable = false)
    @Builder.Default
    private Boolean estado = Boolean.TRUE;

    @OneToMany(mappedBy = "practicante")
    @Builder.Default
    private List<EvaluacionPracticante> evaluaciones = new ArrayList<>();

    @OneToMany(mappedBy = "practicante")
    @Builder.Default
    private List<AtencionClinica> atenciones = new ArrayList<>();
}
