package com.sistemaposjcs.sistemaposjcs.modules.autorizacion.dto;

public record PermisoModuloResponse(
        String codigoModulo,
        boolean reglaConsulta,
        boolean reglaInsercion,
        boolean reglaActualizacion,
        boolean reglaReporte,
        boolean reglaDelete
) {}