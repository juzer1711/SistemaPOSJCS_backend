package com.sistemaposjcs.sistemaposjcs.modules.usuarios.dto;

import com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario;
import com.sistemaposjcs.sistemaposjcs.model.Enum.TipoDocumento;

/** Representación pública de un usuario, sin credenciales ni entidad JPA anidada. */
public record UsuarioResponse(
        Long idUsuario,
        String username,
        String primerNombre,
        String segundoNombre,
        String primerApellido,
        String segundoApellido,
        String nombreCompleto,
        TipoDocumento tipoDocumento,
        String documento,
        String email,
        String telefono,
        Boolean estado
) {
    public static UsuarioResponse from(UsuarioVista usuario) {
        String nombre = java.util.stream.Stream.of(usuario.getPrimerNombre(), usuario.getSegundoNombre(),
                        usuario.getPrimerApellido(), usuario.getSegundoApellido())
                .filter(value -> value != null && !value.isBlank())
                .collect(java.util.stream.Collectors.joining(" "));
        return new UsuarioResponse(usuario.getIdUsuario(), usuario.getUsername(), usuario.getPrimerNombre(),
                usuario.getSegundoNombre(), usuario.getPrimerApellido(), usuario.getSegundoApellido(), nombre,
                usuario.getTipoDocumento(), usuario.getDocumento(), usuario.getEmail(), usuario.getTelefono(), usuario.getEstado());
    }
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getIdUsuario(),
                usuario.getUsername(),
                usuario.getPrimerNombre(),
                usuario.getSegundoNombre(),
                usuario.getPrimerApellido(),
                usuario.getSegundoApellido(),
                usuario.getNombreCompleto(),
                usuario.getTipoDocumento(),
                usuario.getDocumento(),
                usuario.getEmail(),
                usuario.getTelefono(),
                usuario.getEstado()
        );
    }
}
