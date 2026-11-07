package com.sistemaposjcs.sistemaposjcs.controller;

import com.sistemaposjcs.sistemaposjcs.dto.AuditoriaDTO;
import com.sistemaposjcs.sistemaposjcs.model.Auditoria;
import com.sistemaposjcs.sistemaposjcs.service.AuditoriaService;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.core.TypedPropertyPath;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public Page<AuditoriaDTO> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String modulo,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(TypedPropertyPath.of(Auditoria::getFecha)).descending()
        );

        return auditoriaService.listarPaginado(
                pageable,
                search,
                usuario,
                modulo,
                accion,
                fechaInicio,
                fechaFin
        );
    }
}
