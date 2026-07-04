package com.clinica.universitaria.service;

import com.clinica.universitaria.config.MercadoPagoProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MercadoPagoService {

    private static final String API_URL = "https://api.mercadopago.com";

    private final MercadoPagoProperties mpProperties;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> crearPreferencia(Long pagoId, String titulo, BigDecimal precio, String externalReference) {
        String url = API_URL + "/checkout/preferences";

        String successUrl = mpProperties.getSuccessUrl();
        String failureUrl = mpProperties.getFailureUrl();
        String pendingUrl = mpProperties.getPendingUrl();

        log.info("MP Success URL: {}", successUrl);
        log.info("MP Properties: accessToken={}, publicKey={}", 
            mpProperties.getAccessToken() != null ? "SET" : "NULL",
            mpProperties.getPublicKey() != null ? "SET" : "NULL");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(mpProperties.getAccessToken());
        headers.set("X-Idempotency-Key", pagoId.toString());

        Map<String, Object> item = Map.of(
                "title", titulo,
                "quantity", 1,
                "currency_id", "PEN",
                "unit_price", precio
        );

        Map<String, Object> backUrls = Map.of(
                "success", successUrl != null ? successUrl : "http://localhost:8080/pagos/exito",
                "failure", failureUrl != null ? failureUrl : "http://localhost:8080/pagos/fallo",
                "pending", pendingUrl != null ? pendingUrl : "http://localhost:8080/pagos/pendiente"
        );

        Map<String, Object> body = Map.of(
                "items", List.of(item),
                "back_urls", backUrls,
                "external_reference", externalReference
        );

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Preferencia creada: {}", response.getBody().get("id"));
                return response.getBody();
            }
            throw new RuntimeException("Error al crear preferencia: " + response.getStatusCode());
        } catch (Exception e) {
            log.error("Error Mercado Pago", e);
            throw new RuntimeException("Error al crear preferencia de pago: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> consultarPago(String paymentId) {
        String url = API_URL + "/v1/payments/" + paymentId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(mpProperties.getAccessToken());

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return Map.of("status", "error");
        } catch (Exception e) {
            log.error("Error consultando pago {}: {}", paymentId, e.getMessage());
            return Map.of("status", "error");
        }
    }
}
