package com.clinica.universitaria.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "mercado.pago")
public class MercadoPagoProperties {

    private String accessToken;
    private String publicKey;
    private String successUrl;
    private String failureUrl;
    private String pendingUrl;
}
