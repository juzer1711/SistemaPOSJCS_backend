package com.sistemaposjcs.sistemaposjcs.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.sistemaposjcs.sistemaposjcs.config.GeminiProperties;
import com.sistemaposjcs.sistemaposjcs.model.Enum.TipoConsulta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class GeminiIntentClassifierClientTest {
    private MockRestServiceServer server;
    private GeminiIntentClassifierClient client;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://example.invalid");
        server = MockRestServiceServer.bindTo(builder).build();
        GeminiProperties properties = new GeminiProperties();
        properties.setApiKey("clave-sintetica-de-prueba");
        client = new GeminiIntentClassifierClient(builder.build(), properties, new GeminiPromptBuilder());
    }

    @Test
    void conservaLaLecturaDelJsonTipadoYLaLimpiezaDeLaIntencion() {
        server.expect(method(HttpMethod.POST)).andRespond(withSuccess("""
                {"candidates":[{"content":{"parts":[{"text":"`VENTAS_HOY`"}]}}]}
                """, MediaType.APPLICATION_JSON));

        assertEquals(TipoConsulta.VENTAS_HOY, client.clasificar("¿Cuánto vendimos hoy?").orElseThrow());
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}", "{\"candidates\":[]}", "{\"candidates\":[\"formato inesperado\"]}",
            "{\"candidates\":[{\"content\":{\"parts\":[]}}]}"
    })
    void respuestaIncompletaConservaElFallbackSinConversionesInseguras(String response) {
        server.expect(method(HttpMethod.POST)).andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        assertTrue(client.clasificar("¿Cuánto vendimos hoy?").isEmpty());
        server.verify();
    }
}
