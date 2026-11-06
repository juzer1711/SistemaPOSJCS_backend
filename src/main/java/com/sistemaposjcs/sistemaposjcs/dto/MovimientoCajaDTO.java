package com.sistemaposjcs.sistemaposjcs.dto;

import com.sistemaposjcs.sistemaposjcs.model.Enum.TipoMovimiento;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoCajaDTO {

    private Long idMovimiento;

    private Long idCaja;
    private Long idUsuario;

    private TipoMovimiento tipo;  // ENTRADA / SALIDA

    private BigDecimal monto;

    private String motivo;

    private LocalDateTime fecha;
}