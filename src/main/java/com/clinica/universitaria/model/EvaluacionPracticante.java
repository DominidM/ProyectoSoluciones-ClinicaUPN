package com.clinica.universitaria.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "evaluaciones_practicantes")
public class EvaluacionPracticante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evaluacion")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_practicante", nullable = false)
    private Practicante practicante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_doctor_supervisor", nullable = false)
    private Doctor doctorSupervisor;

    @Column(name = "fecha_evaluacion", nullable = false)
    private LocalDate fechaEvaluacion;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal puntaje;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Column(nullable = false)
    @Builder.Default
    private Boolean estado = Boolean.TRUE;

    public String getResultado() {
        return puntaje != null && puntaje.compareTo(BigDecimal.valueOf(14)) >= 0 ? "Aprobado" : "Observado";
    }
}
