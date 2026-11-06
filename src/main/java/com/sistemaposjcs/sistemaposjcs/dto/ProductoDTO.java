package com.sistemaposjcs.sistemaposjcs.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

import com.sistemaposjcs.sistemaposjcs.model.Categoria;
import com.sistemaposjcs.sistemaposjcs.model.Enum.IVA;



@Getter @Setter @AllArgsConstructor
public class ProductoDTO {
    private Long idProducto;
    private String nombre;
    private Categoria categoria;
    private String codigoBarras;
    private String descripcion;
    private BigDecimal costo;
    private BigDecimal precioventa;
    private IVA iva;
    private BigDecimal precioSinIva;
    private Integer stockActual = 0;
    private Integer stockMinimo = 0;
    private Boolean estado = true;
}
