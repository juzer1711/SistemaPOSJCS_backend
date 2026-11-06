package com.sistemaposjcs.sistemaposjcs.model;

import com.sistemaposjcs.sistemaposjcs.model.Enum.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "movimientos_caja")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMovimiento;

    @ManyToOne
    @JoinColumn(name = "id_caja", nullable = false)
    private Caja caja;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimiento tipo; // ENTRADA / SALIDA

    @Column(nullable = false)
    private BigDecimal monto;

    @Column(nullable = false)
    private String motivo;

    private LocalDateTime fecha;

    @PrePersist
    public void prePersist(){
        this.fecha = LocalDateTime.now();
    }
}