package com.sistemaposjcs.sistemaposjcs.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.sistemaposjcs.sistemaposjcs.model.Enum.MetodoPago;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class VentaDTO {
    private Long idVenta;
    private LocalDateTime fecha;
    private Long idCliente;
    private String nombreCliente;
    private String documentoCliente;
    private Long idCaja;
    private Long idUsuario;
    private String nombreCajero;
    private MetodoPago metodoPago;
    private BigDecimal total;
    private BigDecimal totalIVA;
    private BigDecimal totalSinIVA;
    private String observaciones;
    private Boolean estado = true;
    private BigDecimal montoRecibido;
    private BigDecimal cambio;
    private List<ItemFacturaDTO> items;
}
