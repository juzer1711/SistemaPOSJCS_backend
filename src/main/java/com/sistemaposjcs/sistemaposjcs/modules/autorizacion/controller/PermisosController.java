package com.sistemaposjcs.sistemaposjcs.modules.autorizacion.controller;

import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.service.UsuarioAuthService;
import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.dto.PermisoModuloResponse;
import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.service.AutorizacionService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** La identidad siempre procede del JWT; no admite consultar permisos de terceros. */
@RestController
@RequestMapping("/api/auth")
public class PermisosController {
    private final UsuarioAuthService usuarioAuthService;
    private final AutorizacionService autorizacionService;

    public PermisosController(UsuarioAuthService usuarioAuthService, AutorizacionService autorizacionService) {
        this.usuarioAuthService = usuarioAuthService;
        this.autorizacionService = autorizacionService;
    }

    @GetMapping("/permisos")
    public List<PermisoModuloResponse> permisosPropios() {
        return autorizacionService.permisosDeUsuario(usuarioAuthService.getUsuarioLogueado().getUsername());
    }
}