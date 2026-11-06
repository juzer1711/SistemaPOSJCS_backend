package com.sistemaposjcs.sistemaposjcs.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sistemaposjcs.sistemaposjcs.dto.ConsultaRequestDTO;
import com.sistemaposjcs.sistemaposjcs.dto.ConsultaResponseDTO;
import com.sistemaposjcs.sistemaposjcs.service.AsistenteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/asistente")
public class AsistenteController {

    private final AsistenteService asistenteService;

    public AsistenteController(AsistenteService asistenteService) {
        this.asistenteService = asistenteService;
    }

    @PostMapping("/consultar")
    public ResponseEntity<ConsultaResponseDTO> consultar(
            @Valid @RequestBody ConsultaRequestDTO request
    ) {
        return ResponseEntity.ok(
                asistenteService.consultar(request)
        );
    }
}
