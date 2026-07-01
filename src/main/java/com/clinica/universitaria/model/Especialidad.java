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
@Table(name = "especialidades")
public class Especialidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_especialidad")
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(length = 200)
    private String descripcion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean estado = Boolean.TRUE;

    @OneToMany(mappedBy = "especialidad")
    @Builder.Default
    private List<Doctor> doctores = new ArrayList<>();

    @OneToMany(mappedBy = "especialidad")
    @Builder.Default
    private List<Practicante> practicantes = new ArrayList<>();

    @OneToMany(mappedBy = "especialidad")
    @Builder.Default
    private List<Consultorio> consultorios = new ArrayList<>();

    @OneToMany(mappedBy = "especialidad")
    @Builder.Default
    private List<Cita> citas = new ArrayList<>();
}
