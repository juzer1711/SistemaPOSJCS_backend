package com.sistemaposjcs.sistemaposjcs.modules.autenticacion.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

@Getter @Setter
public class LoginRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}