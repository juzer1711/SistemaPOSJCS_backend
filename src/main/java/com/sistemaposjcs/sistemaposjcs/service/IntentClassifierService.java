package com.sistemaposjcs.sistemaposjcs.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sistemaposjcs.sistemaposjcs.config.GeminiProperties;
import com.sistemaposjcs.sistemaposjcs.model.Enum.TipoConsulta;

@Service
public class IntentClassifierService {

    private final GeminiProperties geminiProperties;
    private final GeminiIntentClassifierClient geminiIntentClassifierClient;
    private final RuleBasedIntentClassifierService ruleBasedIntentClassifierService;

    public IntentClassifierService(
            GeminiProperties geminiProperties,
            GeminiIntentClassifierClient geminiIntentClassifierClient,
            RuleBasedIntentClassifierService ruleBasedIntentClassifierService
    ) {
        this.geminiProperties = geminiProperties;
        this.geminiIntentClassifierClient = geminiIntentClassifierClient;
        this.ruleBasedIntentClassifierService = ruleBasedIntentClassifierService;
    }

    public TipoConsulta clasificar(String pregunta) {
        if (!geminiProperties.isEnabled()) {
            return clasificarConReglas(pregunta);
        }

        Optional<TipoConsulta> tipoConsulta = geminiIntentClassifierClient.clasificar(pregunta);

        return tipoConsulta.orElseGet(() -> clasificarConReglas(pregunta));
    }

    private TipoConsulta clasificarConReglas(String pregunta) {
        return ruleBasedIntentClassifierService.clasificar(pregunta);
    }
}
