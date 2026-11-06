package com.sistemaposjcs.sistemaposjcs.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class VentaCajaDTO {

    private Long idVenta;
    private LocalDateTime fecha;
    private String metodoPago;
    private BigDecimal total;

}