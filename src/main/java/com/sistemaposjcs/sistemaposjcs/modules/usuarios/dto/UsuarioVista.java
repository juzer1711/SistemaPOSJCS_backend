package com.sistemaposjcs.sistemaposjcs.modules.usuarios.dto;

import com.sistemaposjcs.sistemaposjcs.model.Enum.TipoDocumento;

/** Proyección cerrada: listados y exportaciones nunca seleccionan password. */
public interface UsuarioVista {
    Long getIdUsuario();
    String getUsername();
    String getPrimerNombre();
    String getSegundoNombre();
    String getPrimerApellido();
    String getSegundoApellido();
    TipoDocumento getTipoDocumento();
    String getDocumento();
    String getEmail();
    String getTelefono();
    Boolean getEstado();
}