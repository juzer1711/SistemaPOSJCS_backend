package com.sistemaposjcs.sistemaposjcs.modules.modulos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ModuloRequest(
        @NotBlank(message = "El código del módulo es obligatorio")
        @Size(max = 50, message = "El código del módulo no puede superar 50 caracteres")
        @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "El código debe usar mayúsculas, números o guion bajo")
        String codigoModulo,

        @NotBlank(message = "El archivo es obligatorio")
        @Size(max = 255, message = "El archivo no puede superar 255 caracteres")
        String archivo,

        @NotBlank(message = "El nombre del módulo es obligatorio")
        @Size(max = 100, message = "El nombre del módulo no puede superar 100 caracteres")
        String nombreModulo,

        @NotBlank(message = "La zona es obligatoria")
        @Size(max = 100, message = "La zona no puede superar 100 caracteres")
        String zona,

        @Size(max = 1000, message = "Los comentarios no pueden superar 1000 caracteres")
        String comentarios
) {
}
