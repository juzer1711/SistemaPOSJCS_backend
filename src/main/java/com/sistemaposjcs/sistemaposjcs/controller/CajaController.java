package com.sistemaposjcs.sistemaposjcs.controller;

import com.sistemaposjcs.sistemaposjcs.dto.CajaDTO;
import com.sistemaposjcs.sistemaposjcs.dto.CerrarCajaDTO;
import com.sistemaposjcs.sistemaposjcs.model.Caja;
import com.sistemaposjcs.sistemaposjcs.service.CajaService;
import com.sistemaposjcs.sistemaposjcs.model.Enum.EstadoCaja;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;


@RestController
@RequestMapping("/api/cajas")
public class CajaController {

    private final CajaService cajaService;

    public CajaController(CajaService cajaService) {
        this.cajaService = cajaService;
    }

    // 🔹 Convertir Caja a DTO
    private CajaDTO convertirCaja(Caja c) {
        return new CajaDTO(
                c.getIdCaja(),
                c.getUsuario().getIdUsuario(),
                c.getUsuario().getNombreCompleto(),
                c.getFechaApertura(),
                c.getFechaCierre(),
                c.getMontoInicial(),
                c.getTotalVentas(),
                c.getTotalEfectivo(),
                c.getTotalTransferencia(),
                c.getMontoFinal(),
                c.getEfectivoReal(),
                c.getTransferenciaReal(),
                c.getDiferenciaEfectivo(),
                c.getDiferenciaTransferencia(),
                c.getEstadoCaja()
        );
    }

    // ✅ 1. Obtener caja por ID
    @GetMapping("/{id}")
    public ResponseEntity<CajaDTO> getCajaById(@PathVariable Long id) {
        Caja caja = cajaService.getCajaById(id);
        return ResponseEntity.ok(convertirCaja(caja));
    }

    // ✅ 2. Listar cajas abiertas
    @GetMapping("/abiertas")
    public Page<CajaDTO> getCajasAbiertas(Pageable pageable) {
        return cajaService.getCajasAbiertas(pageable)
                .map(this::convertirCaja);
    }

    // ✅ 3. Abrir caja
    @PostMapping("/abrir")
    public ResponseEntity<CajaDTO> abrirCaja(@RequestBody CajaDTO dto) {

        Caja caja = cajaService.abrirCaja(
                dto.getIdUsuario(),
                dto.getMontoInicial()
        );

        return ResponseEntity.ok(convertirCaja(caja));
    }

    // ✅ 4. Cerrar caja
    @PostMapping("/cerrar")
    public ResponseEntity<CajaDTO> cerrarCaja(@RequestBody CerrarCajaDTO dto) {

        Caja caja = cajaService.cerrarCaja(
            dto.getIdUsuario(),
            dto.getEfectivoReal(),
            dto.getTransferenciaReal()
        );

        return ResponseEntity.ok(convertirCaja(caja));
    }

    // ✅ 5. Listar cajas cerradas
    @GetMapping("/cerradas")
    public Page<CajaDTO> getCajasCerradas(Pageable pageable) {
        return cajaService.getCajasCerradas(pageable)
                .map(this::convertirCaja);
    }

    // ✅ 6. Obtener caja por IDUsuario
    @GetMapping("/{idUsuario}/activa")
    public ResponseEntity<CajaDTO> getCajaByIdUsuario(@PathVariable Long idUsuario) {
        Caja caja = cajaService.obtenerCajaActivaPorUsuario(idUsuario);
        return ResponseEntity.ok(convertirCaja(caja));
    }

        @GetMapping("/search")
    public Page<CajaDTO> searchCajas(
        Pageable pageable,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Long idCaja,
        @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaApertura,
        @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaCierre,
        @RequestParam(required = false) EstadoCaja estado
    ) {
        return cajaService.searchCajas(
            pageable, search, idCaja, fechaApertura, fechaCierre, estado
        ).map(this::convertirCaja);
    }

    @PostMapping("/cerrar/admin/{idCaja}")
    public ResponseEntity<?> cerrarCajaAdmin(@PathVariable Long idCaja) {
        cajaService.cerrarCajaForzadoAdmin(idCaja);
        return ResponseEntity.ok("Caja cerrada por administrador");
    }

}