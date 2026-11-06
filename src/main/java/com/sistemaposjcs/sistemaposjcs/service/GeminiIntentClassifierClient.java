package com.sistemaposjcs.sistemaposjcs.service;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.sistemaposjcs.sistemaposjcs.config.GeminiProperties;
import com.sistemaposjcs.sistemaposjcs.model.Enum.TipoConsulta;

@Service
public class GeminiIntentClassifierClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeminiIntentClassifierClient.class);

    private final RestClient geminiRestClient;
    private final GeminiProperties geminiProperties;
    private final GeminiPromptBuilder geminiPromptBuilder;

    public GeminiIntentClassifierClient(
            RestClient geminiRestClient,
            GeminiProperties geminiProperties,
            GeminiPromptBuilder geminiPromptBuilder
    ) {
        this.geminiRestClient = geminiRestClient;
        this.geminiProperties = geminiProperties;
        this.geminiPromptBuilder = geminiPromptBuilder;
    }

    public Optional<TipoConsulta> clasificar(String pregunta) {
        if (geminiProperties.getApiKey() == null || geminiProperties.getApiKey().isBlank()) {
            LOGGER.warn("Gemini esta habilitado, pero no se configuro la API key.");
            return Optional.empty();
        }

        try {
            Map<String, Object> response = geminiRestClient
                    .post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1beta/models/{model}:generateContent")
                            .queryParam("key", geminiProperties.getApiKey())
                            .build(geminiProperties.getModel()))
                    .body(construirRequest(pregunta))
                    .retrieve()
                    .body(Map.class);

            return extraerTipoConsulta(response);
        } catch (Exception e) {
            LOGGER.warn("No fue posible clasificar la pregunta con Gemini: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private Map<String, Object> construirRequest(String pregunta) {
        String prompt = geminiPromptBuilder.construirPrompt(pregunta);

        return Map.of(
                "contents", java.util.List.of(
                        Map.of(
                                "role", "user",
                                "parts", java.util.List.of(
                                        Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0,
                        "topP", 0.1,
                        "maxOutputTokens", 20,
                        "responseMimeType", "text/plain"
                )
        );
    }

    @SuppressWarnings("unchecked")
    private Optional<TipoConsulta> extraerTipoConsulta(Map<String, Object> response) {
        if (response == null) {
            return Optional.empty();
        }

        Object candidatesObject = response.get("candidates");
        if (!(candidatesObject instanceof java.util.List<?> candidates) || candidates.isEmpty()) {
            return Optional.empty();
        }

        Object candidateObject = candidates.get(0);
        if (!(candidateObject instanceof Map<?, ?> candidate)) {
            return Optional.empty();
        }

        Object contentObject = candidate.get("content");
        if (!(contentObject instanceof Map<?, ?> content)) {
            return Optional.empty();
        }

        Object partsObject = content.get("parts");
        if (!(partsObject instanceof java.util.List<?> parts) || parts.isEmpty()) {
            return Optional.empty();
        }

        Object partObject = parts.get(0);
        if (!(partObject instanceof Map<?, ?> part)) {
            return Optional.empty();
        }

        Object textObject = part.get("text");
        String texto = textObject == null ? "" : textObject.toString();

        return convertirATipoConsulta(texto);
    }

    private Optional<TipoConsulta> convertirATipoConsulta(String respuestaGemini) {
        String valor = limpiarRespuesta(respuestaGemini);

        try {
            return Optional.of(TipoConsulta.valueOf(valor));
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Gemini devolvio una intencion invalida: {}", valor);
            return Optional.empty();
        }
    }

    private String limpiarRespuesta(String respuestaGemini) {
        if (respuestaGemini == null) {
            return "";
        }

        return respuestaGemini
                .replace("`", "")
                .replace("\"", "")
                .trim();
    }
}
