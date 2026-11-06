package com.sistemaposjcs.sistemaposjcs.controller;

import com.sistemaposjcs.sistemaposjcs.dto.MovimientoCajaDTO;
import com.sistemaposjcs.sistemaposjcs.service.MovimientoCajaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movimientos-caja")
@RequiredArgsConstructor
public class MovimientoCajaController {

    private final MovimientoCajaService service;

    @PostMapping
    public MovimientoCajaDTO registrar(@RequestBody MovimientoCajaDTO dto){
        return service.registrarMovimiento(dto);
    }

    @GetMapping("/caja/{idCaja}")
    public List<MovimientoCajaDTO> listarPorCaja(@PathVariable Long idCaja){
        return service.listarPorCaja(idCaja);
    }
}
