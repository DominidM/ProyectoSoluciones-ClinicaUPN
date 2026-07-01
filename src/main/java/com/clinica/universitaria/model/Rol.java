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
@Table(name = "roles")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private NombreRol nombre;

    @Column(length = 150)
    private String descripcion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean estado = Boolean.TRUE;

    @OneToMany(mappedBy = "rol")
    @Builder.Default
    private List<Usuario> usuarios = new ArrayList<>();
}
