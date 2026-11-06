package com.sistemaposjcs.sistemaposjcs.controller;

import com.sistemaposjcs.sistemaposjcs.dto.MovimientoInventarioResponseDTO;
import com.sistemaposjcs.sistemaposjcs.dto.MovimientoInventarioResquestDTO;
import com.sistemaposjcs.sistemaposjcs.model.Producto;
import com.sistemaposjcs.sistemaposjcs.model.Enum.TipoMovimientoInventario;
import com.sistemaposjcs.sistemaposjcs.service.InventarioService;
import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

import java.time.LocalDateTime;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService ) {
        this.inventarioService = inventarioService;
    }

    // ✅ RF-28 Entrada
    @PostMapping("/entrada")
    public void registrarEntrada(@Valid @RequestBody MovimientoInventarioResquestDTO dto) {
        inventarioService.registrarEntrada(
                dto.getIdProducto(),
                dto.getCantidad(),
                dto.getMotivo()
        );
    }

    // ✅ RF-29 Salida
    @PostMapping("/salida")
    public void registrarSalida(@Valid @RequestBody MovimientoInventarioResquestDTO dto) {
        inventarioService.registrarSalida(
                dto.getIdProducto(),
                dto.getCantidad(),
                dto.getMotivo()
        );
    }

    @GetMapping("/stock/{idProducto}")
    public Integer obtenerStock(@PathVariable Long idProducto) {
        return inventarioService.obtenerStock(idProducto);
    }


    @GetMapping("/movimientos")
    public Page<MovimientoInventarioResponseDTO> buscarMovimientos(  // ← cambia el tipo
        @RequestParam(required = false) String search,
        @RequestParam(required = false) TipoMovimientoInventario tipo,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime fechaInicio,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime fechaFin,
        Pageable pageable
    ) {
        return inventarioService.buscarMovimientos(
            search, tipo, fechaInicio, fechaFin, pageable
        ).map(MovimientoInventarioResponseDTO::from);  // ← mapea aquí
    }

    @GetMapping("/alertas")
    public List<Producto> obtenerAlertasStock() {
        return inventarioService.obtenerProductosBajoStock();
    }

}