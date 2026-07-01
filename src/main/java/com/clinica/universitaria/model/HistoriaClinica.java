package com.clinica.universitaria.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "historias_clinicas")
public class HistoriaClinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historia")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_paciente", nullable = false, unique = true)
    private Paciente paciente;

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDate fechaApertura;

    @Column(columnDefinition = "TEXT")
    private String antecedentes;

    @Column(columnDefinition = "TEXT")
    private String alergias;

    @Column(name = "enfermedades_previas", columnDefinition = "TEXT")
    private String enfermedadesPrevias;

    @Column(name = "observaciones_generales", columnDefinition = "TEXT")
    private String observacionesGenerales;

    @Column(nullable = false)
    @Builder.Default
    private Boolean estado = Boolean.TRUE;

    @OneToMany(mappedBy = "historiaClinica")
    @OrderBy("fechaAtencion DESC")
    @Builder.Default
    private List<AtencionClinica> atenciones = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (fechaApertura == null) {
            fechaApertura = LocalDate.now();
        }
    }
}
