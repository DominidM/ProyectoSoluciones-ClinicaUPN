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
@Table(name = "doctores")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_doctor")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_especialidad", nullable = false)
    private Especialidad especialidad;

    @Column(length = 30)
    private String cmp;

    @Column(length = 100)
    private String experiencia;

    @Column(nullable = false)
    @Builder.Default
    private Boolean estado = Boolean.TRUE;

    @OneToMany(mappedBy = "doctor")
    @Builder.Default
    private List<HorarioDoctor> horarios = new ArrayList<>();

    @OneToMany(mappedBy = "doctor")
    @Builder.Default
    private List<Cita> citas = new ArrayList<>();

    @OneToMany(mappedBy = "doctorSupervisor")
    @Builder.Default
    private List<Practicante> practicantesSupervisados = new ArrayList<>();

    @OneToMany(mappedBy = "doctorSupervisor")
    @Builder.Default
    private List<EvaluacionPracticante> evaluacionesRealizadas = new ArrayList<>();

    @OneToMany(mappedBy = "doctor")
    @Builder.Default
    private List<AtencionClinica> atenciones = new ArrayList<>();
}
