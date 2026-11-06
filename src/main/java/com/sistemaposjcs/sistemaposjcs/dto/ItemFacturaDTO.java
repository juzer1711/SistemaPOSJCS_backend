package com.sistemaposjcs.sistemaposjcs.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemFacturaDTO {
    private Long idProducto;
    private String nombreProducto;
    private int cantidad;
    private BigDecimal precioUnitario; // final con IVA
    private BigDecimal subtotal;       // final con IVA
    private BigDecimal ivaPorcentaje;  // 0.00 - 0.05 - 0.19
    private BigDecimal valorIVA;       // cantidad * (precioConIVA - precioSinIVA)
}
