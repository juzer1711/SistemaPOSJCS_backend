package com.sistemaposjcs.sistemaposjcs.controller;

import com.sistemaposjcs.sistemaposjcs.model.Enum.EstadoCaja;
import com.sistemaposjcs.sistemaposjcs.service.ExportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    // ── VENTAS EXCEL ──────────────────────────────────────────────────
    @GetMapping("/ventas/excel")
    public ResponseEntity<byte[]> ventasExcel(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String metodoPago,
            @RequestParam(required = false) Boolean estado,
            @RequestParam(required = false) Long idCaja,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) throws IOException {

        byte[] bytes = exportService.exportVentasExcel(
            search, metodoPago, estado, idCaja, fechaInicio, fechaFin
        );

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ventas.xlsx")
            .contentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(bytes);
    }

    // ── VENTAS CSV ────────────────────────────────────────────────────
    @GetMapping("/ventas/csv")
    public ResponseEntity<byte[]> ventasCSV(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String metodoPago,
            @RequestParam(required = false) Boolean estado,
            @RequestParam(required = false) Long idCaja,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        byte[] bytes = exportService.exportVentasCSV(
            search, metodoPago, estado, idCaja, fechaInicio, fechaFin
        );

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ventas.csv")
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(bytes);
    }

    // ── CAJAS EXCEL ───────────────────────────────────────────────────
@GetMapping("/cajas/excel")
public ResponseEntity<byte[]> cajasExcel(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Long idCaja,
        @RequestParam(required = false) String estado,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaApertura,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaCierre
) throws IOException {

    EstadoCaja estadoCaja = (estado != null && !estado.isEmpty())
        ? EstadoCaja.valueOf(estado)
        : null;

    byte[] bytes = exportService.exportCajasExcel(
        search, idCaja, fechaApertura, fechaCierre, estadoCaja
    );

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cajas.xlsx")
        .contentType(MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .body(bytes);
}

    // ── CAJAS CSV ─────────────────────────────────────────────────────
    @GetMapping("/cajas/csv")
    public ResponseEntity<byte[]> cajasCSV(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long idCaja,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaApertura,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaCierre
    ) {
        EstadoCaja estadoCaja = (estado != null && !estado.isEmpty())
            ? EstadoCaja.valueOf(estado)
            : null;

        byte[] bytes = exportService.exportCajasCSV(
            search, idCaja, fechaApertura, fechaCierre, estadoCaja
        );

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cajas.csv")
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(bytes);
    }

    // ── CLIENTES ──────────────────────────────────────────────────────
@GetMapping("/clientes/excel")
public ResponseEntity<byte[]> clientesExcel(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String tipoCliente,
        @RequestParam(required = false) String tipoDocumento,
        @RequestParam(required = false) Boolean estado
) throws IOException {
    byte[] bytes = exportService.exportClientesExcel(search, tipoCliente, tipoDocumento, estado);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=clientes.xlsx")
        .contentType(MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .body(bytes);
}

@GetMapping("/clientes/csv")
public ResponseEntity<byte[]> clientesCSV(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String tipoCliente,
        @RequestParam(required = false) String tipoDocumento,
        @RequestParam(required = false) Boolean estado
) {
    byte[] bytes = exportService.exportClientesCSV(search, tipoCliente, tipoDocumento, estado);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=clientes.csv")
        .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
        .body(bytes);
}

// ── PRODUCTOS ─────────────────────────────────────────────────────
@GetMapping("/productos/excel")
public ResponseEntity<byte[]> productosExcel(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Long categoria,
        @RequestParam(required = false) Boolean estado
) throws IOException {
    byte[] bytes = exportService.exportProductosExcel(search, categoria, estado);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=productos.xlsx")
        .contentType(MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .body(bytes);
}

@GetMapping("/productos/csv")
public ResponseEntity<byte[]> productosCSV(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Long categoria,
        @RequestParam(required = false) Boolean estado
) {
    byte[] bytes = exportService.exportProductosCSV(search, categoria, estado);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=productos.csv")
        .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
        .body(bytes);
}

@GetMapping("/auditoria/excel")
public ResponseEntity<byte[]> auditoriaExcel(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String usuario,
        @RequestParam(required = false) String modulo,
        @RequestParam(required = false) String accion,
        @RequestParam(required = false) String fechaInicio,
        @RequestParam(required = false) String fechaFin
) throws IOException {
    byte[] bytes = exportService.exportAuditoriaExcel(
        search, usuario, modulo, accion, fechaInicio, fechaFin
    );

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=auditoria.xlsx")
        .contentType(MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .body(bytes);
}

@GetMapping("/auditoria/csv")
public ResponseEntity<byte[]> auditoriaCSV(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String usuario,
        @RequestParam(required = false) String modulo,
        @RequestParam(required = false) String accion,
        @RequestParam(required = false) String fechaInicio,
        @RequestParam(required = false) String fechaFin
) {
    byte[] bytes = exportService.exportAuditoriaCSV(
        search, usuario, modulo, accion, fechaInicio, fechaFin
    );

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=auditoria.csv")
        .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
        .body(bytes);
}
}
