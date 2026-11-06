package com.sistemaposjcs.sistemaposjcs.dto;
import java.math.BigDecimal;
import java.util.List;

import com.sistemaposjcs.sistemaposjcs.model.Enum.MetodoPago;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VentaRequestDTO {

    private Long idCliente;

    private Long idUsuario;

    private MetodoPago metodoPago;

    private BigDecimal montoRecibido;

    private String observaciones;

    private List<ItemVentaRequestDTO> items;
}