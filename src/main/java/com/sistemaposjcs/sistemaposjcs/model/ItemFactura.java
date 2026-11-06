package com.sistemaposjcs.sistemaposjcs.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "items_factura")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemFactura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_venta")
    @JsonBackReference
    private Venta venta;

    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false)
    private BigDecimal precioUnitario; // Precio final (incluye IVA)

    @Column(nullable = false)
    private BigDecimal subtotal; // precioUnitario * cantidad

    @Column(nullable = false)
    private BigDecimal ivaPorcentaje; 

    @Column(nullable = false)
    private BigDecimal valorIVA; // total IVA del ítem

    public ItemFactura(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;

        // Precio final
        this.precioUnitario = producto.getPrecioventa();

        // % IVA
        this.ivaPorcentaje = producto.getIva().getValue(); // 0.19

        // Subtotal = precio final * cantidad
        this.subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));

        // IVA Correcto: (precio final - precio sin IVA) * cantidad
        BigDecimal ivaUnitario = producto.getPrecioventa().subtract(producto.getPrecioSinIva());

        this.valorIVA = ivaUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

}
