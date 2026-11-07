package com.sistemaposjcs.sistemaposjcs.modules.empresa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "empresa",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_empresa_nit", columnNames = "nit"),
                @UniqueConstraint(name = "uk_empresa_instalacion", columnNames = "instalacion_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empresa")
    private Long idEmpresa;

    @Column(name = "instalacion_id", nullable = false, updatable = false, columnDefinition = "integer default 1")
    private Integer instalacionId = 1;

    @Column(name = "nombre_comercial", nullable = false, length = 150)
    private String nombreComercial;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(name = "nit", nullable = false, length = 20)
    private String nit;

    @Column(name = "digito_verificacion", length = 1)
    private String digitoVerificacion;

    @Column(name = "direccion", nullable = false, length = 255)
    private String direccion;

    @Column(name = "municipio", length = 100)
    private String municipio;

    @Column(name = "departamento", length = 100)
    private String departamento;

    @Column(name = "telefono", nullable = false, length = 20)
    private String telefono;

    @Column(name = "correo", length = 150)
    private String correo;

    // null indica que la condicion tributaria aun no se ha configurado.
    @Column(name = "responsable_iva")
    private Boolean responsableIva;

    @Column(name = "logo", length = 500)
    private String logo;

    @Column(name = "logo_public_id", length = 255)
    private String logoPublicId;

    @Column(name = "estado", nullable = false)
    private Boolean estado = true;
}
