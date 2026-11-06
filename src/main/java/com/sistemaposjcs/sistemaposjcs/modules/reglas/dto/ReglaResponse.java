package com.sistemaposjcs.sistemaposjcs.modules.reglas.dto;

import com.sistemaposjcs.sistemaposjcs.modules.reglas.model.Regla;

public record ReglaResponse(
        Long idRegla,
        Long usuarioId,
        Long moduloId,
        boolean reglaConsulta,
        boolean reglaInsercion,
        boolean reglaActualizacion,
        boolean reglaReporte,
        boolean reglaDelete
) {
    public static ReglaResponse from(Regla regla) {
        return new ReglaResponse(
                regla.getIdRegla(),
                regla.getUsuario().getIdUsuario(),
                regla.getModulo().getIdModulo(),
                regla.isReglaConsulta(),
                regla.isReglaInsercion(),
                regla.isReglaActualizacion(),
                regla.isReglaReporte(),
                regla.isReglaDelete()
        );
    }
}
