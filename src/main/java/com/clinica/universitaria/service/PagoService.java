package com.clinica.universitaria.service;

import com.clinica.universitaria.model.Cita;
import com.clinica.universitaria.model.EstadoPago;
import com.clinica.universitaria.model.Pago;
import com.clinica.universitaria.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PagoService {

    private final PagoRepository pagoRepository;
    private final MercadoPagoService mercadoPagoService;

    private static final BigDecimal MONTO_POR_CITA = new BigDecimal("10.00");

    public Pago crearOPagarCita(Cita cita) {
        return pagoRepository.findByCitaId(cita.getId())
                .orElseGet(() -> {
                    Pago pago = Pago.builder()
                            .cita(cita)
                            .monto(MONTO_POR_CITA)
                            .estado(EstadoPago.PENDIENTE)
                            .build();
                    pago = pagoRepository.save(pago);
                    try {
                        return generarPreferencia(pago);
                    } catch (Exception e) {
                        log.error("No se pudo crear preferencia MP para cita {}: {}", cita.getId(), e.getMessage());
                        return pago;
                    }
                });
    }

    public Pago generarPreferencia(Pago pago) {
        String titulo = "Cita medica - " + pago.getCita().getEspecialidad().getNombre();
        String externalRef = "cita-" + pago.getCita().getId() + "-pago-" + pago.getId();

        Map<String, Object> preferencia = mercadoPagoService.crearPreferencia(
                pago.getId(), titulo, pago.getMonto(), externalRef
        );

        pago.setPreferenceId((String) preferencia.get("id"));
        return pagoRepository.save(pago);
    }

    @Transactional(readOnly = true)
    public String getInitPoint(Long citaId) {
        return pagoRepository.findByCitaId(citaId)
                .filter(p -> p.getPreferenceId() != null)
                .map(p -> "https://www.mercadopago.com.pe/checkout/v1/redirect?pref_id=" + p.getPreferenceId())
                .orElse(null);
    }

    public void procesarNotificacion(String preferenceId) {
        pagoRepository.findByPreferenceId(preferenceId).ifPresent(pago -> {
            Map<String, Object> preferencia = mercadoPagoService.consultarPago(preferenceId);

            // Si hay payments, tomar el primero
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> payments = (List<Map<String, Object>>) preferencia.getOrDefault("payments", List.of());

            if (!payments.isEmpty()) {
                String paymentId = (String) payments.get(0).get("id");
                String status = (String) payments.get(0).get("status");

                pago.setPaymentId(paymentId);

                switch (status) {
                    case "approved":
                        pago.setEstado(EstadoPago.APROBADO);
                        log.info("Pago aprobado: cita={}, pago={}", pago.getCita().getId(), paymentId);
                        break;
                    case "rejected":
                        pago.setEstado(EstadoPago.RECHAZADO);
                        break;
                    case "cancelled":
                        pago.setEstado(EstadoPago.CANCELADO);
                        break;
                    default:
                        break;
                }
            }

            pago.setFechaActualizacion(LocalDateTime.now());
            pagoRepository.save(pago);
        });
    }

    public void actualizarDesdeCallback(Long citaId, String status) {
        pagoRepository.findByCitaId(citaId).ifPresent(pago -> {
            switch (status) {
                case "approved":
                    pago.setEstado(EstadoPago.APROBADO);
                    break;
                case "rejected":
                    pago.setEstado(EstadoPago.RECHAZADO);
                    break;
                case "cancelled":
                    pago.setEstado(EstadoPago.CANCELADO);
                    break;
                default:
                    break;
            }
            pago.setFechaActualizacion(LocalDateTime.now());
            pagoRepository.save(pago);
        });
    }

    @Transactional(readOnly = true)
    public Optional<Pago> obtenerPorCita(Long citaId) {
        return pagoRepository.findByCitaId(citaId);
    }

    @Transactional(readOnly = true)
    public List<Pago> obtenerPorPaciente(Long pacienteId) {
        return pagoRepository.findByCitaPacienteId(pacienteId);
    }
}
