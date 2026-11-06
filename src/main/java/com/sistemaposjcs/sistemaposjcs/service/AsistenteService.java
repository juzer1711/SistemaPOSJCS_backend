package com.sistemaposjcs.sistemaposjcs.service;

import org.springframework.stereotype.Service;

import com.sistemaposjcs.sistemaposjcs.dto.ConsultaRequestDTO;
import com.sistemaposjcs.sistemaposjcs.dto.ConsultaResponseDTO;
import com.sistemaposjcs.sistemaposjcs.model.Enum.TipoConsulta;

@Service
public class AsistenteService {

    private final IntentClassifierService intentClassifierService;
    private final QueryExecutorService queryExecutorService;

    public AsistenteService(
            IntentClassifierService intentClassifierService,
            QueryExecutorService queryExecutorService
    ) {
        this.intentClassifierService = intentClassifierService;
        this.queryExecutorService = queryExecutorService;
    }

    public ConsultaResponseDTO consultar(ConsultaRequestDTO request) {

        TipoConsulta tipoConsulta = intentClassifierService.clasificar(
                request.getPregunta()
        );

        String respuesta = queryExecutorService.ejecutar(
                tipoConsulta,
                request.getPregunta()
        );

        return new ConsultaResponseDTO(
                request.getPregunta(),
                tipoConsulta,
                respuesta
        );
    }
}
