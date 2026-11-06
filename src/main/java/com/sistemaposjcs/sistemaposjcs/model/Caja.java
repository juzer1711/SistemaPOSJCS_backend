package com.sistemaposjcs.sistemaposjcs.model;

import com.sistemaposjcs.sistemaposjcs.model.Enum.EstadoCaja;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cajas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Caja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_caja")
    private Long idCaja;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cajero", nullable = false)
    private Usuario usuario;

    @Column()
    private LocalDateTime fechaApertura;
    @Column()
    private LocalDateTime fechaCierre;
    @Column(nullable = false)
    private BigDecimal montoInicial = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal totalVentas = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal totalEfectivo = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal totalTransferencia = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal montoFinal = BigDecimal.ZERO;

    @Column(nullable = true)
    private BigDecimal efectivoReal = BigDecimal.ZERO;

    @Column(nullable = true)
    private BigDecimal transferenciaReal = BigDecimal.ZERO;

    @Column(nullable = true)
    private BigDecimal diferenciaEfectivo = BigDecimal.ZERO;

    @Column(nullable = true)
    private BigDecimal diferenciaTransferencia = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCaja estadoCaja;

    @PrePersist
    public void prePersist() {
        this.fechaApertura = LocalDateTime.now();
        this.estadoCaja = EstadoCaja.ABIERTA;
    }
}
