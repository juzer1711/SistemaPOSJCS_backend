package com.sistemaposjcs.sistemaposjcs.modules.reglas.dto;

import jakarta.validation.constraints.NotNull;

public record ReglaRequest(
        @NotNull(message = "El usuario es obligatorio")
        Long usuarioId,
        @NotNull(message = "El módulo es obligatorio")
        Long moduloId,
        @NotNull(message = "Debe indicar el permiso de consulta")
        Boolean reglaConsulta,
        @NotNull(message = "Debe indicar el permiso de inserción")
        Boolean reglaInsercion,
        @NotNull(message = "Debe indicar el permiso de actualización")
        Boolean reglaActualizacion,
        @NotNull(message = "Debe indicar el permiso de reporte")
        Boolean reglaReporte,
        @NotNull(message = "Debe indicar el permiso de eliminación")
        Boolean reglaDelete
) {
}
