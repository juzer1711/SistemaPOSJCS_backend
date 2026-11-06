package com.sistemaposjcs.sistemaposjcs.dto;

import com.sistemaposjcs.sistemaposjcs.model.Enum.TipoCliente;
import com.sistemaposjcs.sistemaposjcs.model.Enum.TipoDocumento;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {

    private Long idCliente;

    private TipoCliente tipoCliente;

    // Persona natural
    private String primerNombre;
    private String segundoNombre;
    private String primerApellido;
    private String segundoApellido;

    // Empresa
    private String razonSocial;
    private String identificadorNit; // DV

    // Comunes
    private TipoDocumento tipoDocumento;
    private String documento;
    private String email;
    private String telefono;
    private String direccion;

    private Boolean estado;
}
