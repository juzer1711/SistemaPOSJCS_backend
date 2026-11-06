package com.sistemaposjcs.sistemaposjcs.modules.usuarios.model;

import com.sistemaposjcs.sistemaposjcs.model.Enum.TipoDocumento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

/** Usuario persistido; las reglas de entrada HTTP pertenecen a los DTO del módulo. */
@Entity
@Table(
    name = "usuarios",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_documento", columnNames = "documento"),
        @UniqueConstraint(name = "uk_email", columnNames = "email")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String primerNombre;

    @Column(nullable = true)
    private String segundoNombre;

    @Column(nullable = false)
    private String primerApellido;

    @Column(nullable = true)
    private String segundoApellido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDocumento tipoDocumento;


    @Column(nullable = false, unique = true)
    private String documento;

    @Column(unique = true)
    private String email;

    @Column(length = 15)
    private String telefono;

    private Boolean estado = true;

    public String getNombreCompleto(){

        String nombre = primerNombre;

        if(segundoNombre != null && !segundoNombre.isEmpty()){
            nombre += " " + segundoNombre;
        }

        nombre += " " + primerApellido;

        if(segundoApellido != null && !segundoApellido.isEmpty()){
            nombre += " " + segundoApellido;
        }

        return nombre;
    }
}

