package com.sistemaposjcs.sistemaposjcs.modules.reglas.dto;

import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.model.PermisoAccion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PermisoCambioRequest(
        @NotNull(message = "El usuario es obligatorio") @Positive(message = "El usuario debe ser válido") Long usuarioId,
        @NotNull(message = "El módulo es obligatorio") @Positive(message = "El módulo debe ser válido") Long moduloId,
        @NotNull(message = "La acción es obligatoria") PermisoAccion accion,
        @NotNull(message = "El valor del permiso es obligatorio") Boolean valor
) {}