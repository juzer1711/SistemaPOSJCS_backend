package com.sistemaposjcs.sistemaposjcs.modules.reglas.model;

import com.sistemaposjcs.sistemaposjcs.modules.modulos.model.Modulo;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "reglas",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reglas_usuario_modulo",
                columnNames = {"usuario_id", "modulo_id"}
        ),
        indexes = @Index(name = "idx_reglas_modulo", columnList = "modulo_id")
)
@Getter
@Setter
@NoArgsConstructor
public class Regla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_regla")
    private Long idRegla;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modulo_id", nullable = false)
    private Modulo modulo;

    @Column(name = "regla_consulta", nullable = false, columnDefinition = "boolean default false")
    private boolean reglaConsulta;

    @Column(name = "regla_insercion", nullable = false, columnDefinition = "boolean default false")
    private boolean reglaInsercion;

    @Column(name = "regla_actualizacion", nullable = false, columnDefinition = "boolean default false")
    private boolean reglaActualizacion;

    @Column(name = "regla_reporte", nullable = false, columnDefinition = "boolean default false")
    private boolean reglaReporte;

    @Column(name = "regla_delete", nullable = false, columnDefinition = "boolean default false")
    private boolean reglaDelete;
}
