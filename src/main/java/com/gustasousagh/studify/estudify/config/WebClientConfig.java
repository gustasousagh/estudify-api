package com.gustasousagh.studify.estudify.config;

import io.netty.resolver.DefaultAddressResolverGroup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    WebClient webClient(@Value("${gemini.base-url}") String baseUrl,
                        @Value("${gemini.api-key}") String apiKey) {

        ConnectionProvider pool = ConnectionProvider.builder("gemini-pool")
                .maxConnections(200)                  // ajuste conforme carga
                .pendingAcquireMaxCount(2000)
                .pendingAcquireTimeout(Duration.ofSeconds(10))
                .build();

        HttpClient httpClient = HttpClient.create(pool)      // <<-- em vez de runOn(...)
                .responseTimeout(Duration.ofSeconds(65))
                .compress(true)
                .resolver(DefaultAddressResolverGroup.INSTANCE);

        // .wiretap(true)         // opcional (log de rede); algumas versões usam wiretap(true)
        // .metrics(true);        // opcional; pode não existir na sua versão

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-goog-api-key", apiKey)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
