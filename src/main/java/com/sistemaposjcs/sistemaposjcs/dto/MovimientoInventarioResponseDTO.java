package com.sistemaposjcs.sistemaposjcs.dto;

import com.sistemaposjcs.sistemaposjcs.model.MovimientoInventario;
import java.time.LocalDateTime;

public record MovimientoInventarioResponseDTO(
    Long id,
    String nombreProducto,
    Integer cantidad,
    String tipo,
    LocalDateTime fecha,
    String motivo
) {
    public static MovimientoInventarioResponseDTO from(MovimientoInventario m) {
        return new MovimientoInventarioResponseDTO(
            m.getId(),
            m.getProducto() != null ? m.getProducto().getNombre() : "—",
            m.getCantidad(),
            m.getTipo() != null ? m.getTipo().name() : null,
            m.getFecha(),
            m.getMotivo()
        );
    }
}