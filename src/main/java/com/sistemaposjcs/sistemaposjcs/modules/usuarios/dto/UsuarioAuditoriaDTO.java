package com.sistemaposjcs.sistemaposjcs.modules.usuarios.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Datos mínimos del usuario incluidos en las respuestas de auditoría. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioAuditoriaDTO {

    private Long idUsuario;
    private String username;
    private String nombreCompleto;
}
