package com.sistemaposjcs.sistemaposjcs.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sistemaposjcs.sistemaposjcs.dto.ReporteResumenDTO;
import com.sistemaposjcs.sistemaposjcs.service.ReporteService;
import com.sistemaposjcs.sistemaposjcs.dto.ReporteMetodoPagoDTO;
import com.sistemaposjcs.sistemaposjcs.dto.ReporteProductoDTO;
import com.sistemaposjcs.sistemaposjcs.dto.ReporteCajeroDTO;
import com.sistemaposjcs.sistemaposjcs.dto.ReporteStockBajoDTO;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/resumen")
    public ResponseEntity<ReporteResumenDTO> obtenerResumen() {
        return ResponseEntity.ok(
                reporteService.obtenerResumen()
        );
    }

    @GetMapping("/metodos-pago")
    public ResponseEntity<List<ReporteMetodoPagoDTO>> obtenerVentasPorMetodoPago() {

        return ResponseEntity.ok(
                reporteService.obtenerVentasPorMetodoPago()
        );
    }

    @GetMapping("/top-productos")
    public ResponseEntity<List<ReporteProductoDTO>> obtenerTopProductosVendidos() {

        return ResponseEntity.ok(
                reporteService.obtenerTopProductosVendidos()
        );
    }

    @GetMapping("/ventas-cajero")
    public ResponseEntity<List<ReporteCajeroDTO>> obtenerVentasPorCajero() {

        return ResponseEntity.ok(
                reporteService.obtenerVentasPorCajero()
        );
    }

    @GetMapping("/stock")
    public ResponseEntity<List<ReporteStockBajoDTO>> getStock(
            @RequestParam(required = false) String tipo
    ) {
        return ResponseEntity.ok(
                reporteService.obtenerStock(tipo)
        );
    }
}