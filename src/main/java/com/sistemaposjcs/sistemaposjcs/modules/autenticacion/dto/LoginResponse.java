package com.sistemaposjcs.sistemaposjcs.modules.autenticacion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class LoginResponse {
    private String status;  // "ok" o "error"
    private Long idUsuario;
    private String username;
    private String message;
    private String token;
}
