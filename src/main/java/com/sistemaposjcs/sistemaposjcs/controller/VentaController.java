package com.sistemaposjcs.sistemaposjcs.controller;

import com.sistemaposjcs.sistemaposjcs.dto.ItemFacturaDTO;
import com.sistemaposjcs.sistemaposjcs.dto.VentaCajaDTO;
import com.sistemaposjcs.sistemaposjcs.dto.VentaDTO;
import com.sistemaposjcs.sistemaposjcs.model.Venta;
import com.sistemaposjcs.sistemaposjcs.model.Enum.MetodoPago;
import com.sistemaposjcs.sistemaposjcs.service.VentaService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")

public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    // 🔹 Convertir entidad → DTO
    private VentaDTO convertirVenta(Venta v) {

        List<ItemFacturaDTO> itemsDTO = v.getItems().stream()
                .map(i -> new ItemFacturaDTO(
                        i.getProducto().getIdProducto(),
                        i.getProducto().getNombre(),
                        i.getCantidad(),
                        i.getPrecioUnitario(),
                        i.getSubtotal(),
                        i.getIvaPorcentaje(),
                        i.getValorIVA()
                )).toList();

        return new VentaDTO(
                v.getIdVenta(),
                v.getFecha(),
                v.getCliente().getIdCliente(),
                v.getCliente().getNombreCompleto(),
                v.getCliente().getDocumento(),
                v.getCaja().getIdCaja(),          
                v.getUsuario().getIdUsuario(),
                v.getUsuario().getNombreCompleto(),
                v.getMetodoPago(),
                v.getTotal(),
                v.getTotalIVA(),
                v.getTotalSinIVA(),
                v.getObservaciones(),
                v.getEstado(),
                v.getMontoRecibido(),
                v.getCambio(),
                itemsDTO
        );
    }

    // ✅ 1. ventas activas
    @GetMapping
    public Page<VentaDTO> getActiveVentas(Pageable pageable) {

        return ventaService.getActiveVentas(pageable)
                .map(this::convertirVenta);
    } 
    

    // ✅ 2. ventas inactivas
    @GetMapping("/inactivas")
    public Page<VentaDTO> getInactiveVentas(Pageable pageable) {

        return ventaService.getInactiveVentas(pageable)
                .map(this::convertirVenta);
    }

    // ✅ 3. Obtener venta por ID
    @GetMapping("/{id}")
    public ResponseEntity<VentaDTO> getVentaById(@PathVariable Long id) {

        Venta venta = ventaService.getVentaById(id);

        return ResponseEntity.ok(convertirVenta(venta));
    }

    // ✅ 4. Crear venta
    @PostMapping
    public ResponseEntity<VentaDTO> createVenta(
            @Valid @RequestBody Venta venta
    ) {

        Venta nuevaVenta = ventaService.createVenta(venta);

        return ResponseEntity.ok(convertirVenta(nuevaVenta));
    }

    // ✅ 6. Desactivar venta (anular)
    @PatchMapping("/desactivar/{id}")
    public ResponseEntity<Void> desactivarVenta(@PathVariable Long id) {

        ventaService.desactivarVenta(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ 7. Reactivar venta
    @PatchMapping("/activar/{id}")
    public ResponseEntity<VentaDTO> activarVenta(@PathVariable Long id) {

        Venta venta = ventaService.activarVenta(id);

        return ResponseEntity.ok(convertirVenta(venta));
    }

    @GetMapping("/search")
    public Page<VentaDTO> searchVentas(
        Pageable pageable,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String metodoPago,
        @RequestParam(required = false) Boolean estado,
        @RequestParam(required = false) Long idCaja,
        @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
        @RequestParam(required = false) 
        @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin
    ) {
        return ventaService.searchVentas(
            pageable, search, metodoPago, estado, idCaja, fechaInicio, fechaFin
        ).map(this::convertirVenta);
    }

    @GetMapping("/por-caja/{idCaja}")
    public ResponseEntity<List<VentaCajaDTO>> obtenerVentasPorCaja(@PathVariable Long idCaja) {
        return ResponseEntity.ok(ventaService.obtenerVentasPorCaja(idCaja));
    }

    @PatchMapping("/{id}/metodo-pago")
    public ResponseEntity<VentaDTO> cambiarMetodoPago(
            @PathVariable Long id,
            @RequestParam MetodoPago metodoPago
    ) {
        Venta v = ventaService.cambiarMetodoPago(id, metodoPago);
        return ResponseEntity.ok(convertirVenta(v));
    }

}
