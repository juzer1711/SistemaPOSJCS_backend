package com.sistemaposjcs.sistemaposjcs.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sistemaposjcs.sistemaposjcs.model.Enum.EstadoCaja;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CajaDTO {
    private Long idCaja;
    private Long idUsuario;
    private String nombreCajero;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    private BigDecimal montoInicial;
    private BigDecimal totalVentas;
    private BigDecimal totalEfectivo;
    private BigDecimal totalTransferencia;
    private BigDecimal montoFinal;
    private BigDecimal efectivoReal;
    private BigDecimal transferenciaReal;
    private BigDecimal diferenciaEfectivo;
    private BigDecimal diferenciaTransferencia;
    private EstadoCaja estadoCaja;
}