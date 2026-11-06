package com.sistemaposjcs.sistemaposjcs.modules.modulos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "modulos",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_modulos_nombre_modulo",
                columnNames = "nombre_modulo"
        ),
        indexes = {
                @Index(name = "idx_modulos_nombre_modulo", columnList = "nombre_modulo"),
                @Index(name = "idx_modulos_zona", columnList = "zona"),
                @Index(name = "uk_modulos_codigo_modulo", columnList = "codigo_modulo", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Modulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_modulo")
    private Long idModulo;

    @Column(name = "codigo_modulo", nullable = false, length = 50)
    private String codigoModulo;

    @Column(name = "archivo", nullable = false, length = 255)
    private String archivo;

    @Column(name = "nombre_modulo", nullable = false, length = 100)
    private String nombreModulo;

    @Column(name = "zona", nullable = false, length = 100)
    private String zona;

    @Column(name = "comentarios", length = 1000)
    private String comentarios;
}
