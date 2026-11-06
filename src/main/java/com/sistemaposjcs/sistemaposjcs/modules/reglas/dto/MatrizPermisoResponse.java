package com.sistemaposjcs.sistemaposjcs.modules.reglas.dto;

public record MatrizPermisoResponse(
        Long idRegla,
        Long moduloId,
        String codigoModulo,
        String nombreModulo,
        boolean reglaConsulta,
        boolean reglaInsercion,
        boolean reglaActualizacion,
        boolean reglaReporte,
        boolean reglaDelete
) {}