package com.sistemaposjcs.sistemaposjcs.model;

import com.sistemaposjcs.sistemaposjcs.model.Enum.IVA;

import java.math.BigDecimal;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;


@Entity
@Table(
    name = "productos",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_codigo_barras", columnNames = "codigoBarras")
    }   
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long idProducto;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 100, message = "El nombre no debe exceder 100 caracteres")
    @Column(nullable = false)
    private String nombre;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @NotBlank(message = "El codigo de barras del producto es obligatorio")
    @Pattern(
        regexp = "^[0-9]{6,20}$",
        message = "El codigo de barras debe contener solo números (6 a 20 dígitos)"
    )
    @Column(nullable = false, unique = true)
    private String codigoBarras;

    @Size(max = 500, message = "La descripcion no debe exceder 500 caracteres")
    @Column(nullable = false)
    private String descripcion;


    @Positive(message = "El costo debe ser mayor que cero")
    @Column(nullable = false)
    private BigDecimal costo;

    @Positive(message = "El precio debe ser mayor que cero")
    @Column(nullable = false)
    private BigDecimal precioventa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IVA iva;

    @Column(nullable = false)
    private BigDecimal precioSinIva;

    @Column(nullable = false)
    private Integer stockActual;

    @Column(nullable = false)
    private Integer stockMinimo;

    @Column(nullable = false)
    private Boolean estado;

}
