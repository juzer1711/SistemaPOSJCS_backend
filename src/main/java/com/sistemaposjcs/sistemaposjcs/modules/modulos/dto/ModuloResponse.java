package com.sistemaposjcs.sistemaposjcs.modules.modulos.dto;

import com.sistemaposjcs.sistemaposjcs.modules.modulos.model.Modulo;

public record ModuloResponse(
        Long idModulo,
        String codigoModulo,
        String archivo,
        String nombreModulo,
        String zona,
        String comentarios
) {
    public static ModuloResponse from(Modulo modulo) {
        return new ModuloResponse(
                modulo.getIdModulo(),
                modulo.getCodigoModulo(),
                modulo.getArchivo(),
                modulo.getNombreModulo(),
                modulo.getZona(),
                modulo.getComentarios()
        );
    }
}
