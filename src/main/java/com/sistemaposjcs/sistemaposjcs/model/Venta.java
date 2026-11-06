package com.sistemaposjcs.sistemaposjcs.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sistemaposjcs.sistemaposjcs.model.Enum.MetodoPago;

import com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "ventas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Long idVenta;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_caja", nullable = false)
    private Caja caja;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodoPago metodoPago;

    @Column(nullable = false)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal totalIVA = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal totalSinIVA = BigDecimal.ZERO;

    @Column(length = 500)
    private String observaciones;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ItemFactura> items = new ArrayList<>();

    @Column(nullable = false)
    private Boolean estado = true;

    @Column(nullable = true)
    private BigDecimal montoRecibido;

    @Column(nullable = true)
    private BigDecimal cambio;


    public void addItem(ItemFactura item) {
        item.setVenta(this);
        items.add(item);
        total = total.add(item.getSubtotal()); // Recalcula total
        totalIVA = totalIVA.add(item.getValorIVA());
        totalSinIVA = totalSinIVA.add(
        item.getSubtotal().subtract(item.getValorIVA())
        ); 
    }
    @PrePersist
    public void prePersist() {
        this.fecha = LocalDateTime.now();
    }
}
