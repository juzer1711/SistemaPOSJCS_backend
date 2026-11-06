package com.sistemaposjcs.sistemaposjcs.modules.usuarios.controller;

import com.sistemaposjcs.sistemaposjcs.modules.usuarios.dto.UsuarioRequest;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.dto.UsuarioResponse;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.service.UsuarioService;
import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.annotation.ModuloProtegido;
import com.sistemaposjcs.sistemaposjcs.model.Enum.TipoDocumento;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Endpoints REST y filtros paginados del módulo Usuario. */
@RestController
@RequestMapping("/api/users")
@ModuloProtegido("USUARIOS")
public class UsuarioController {

    private final UsuarioService userService;

    public UsuarioController(UsuarioService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Page<UsuarioResponse> getActiveUsers(Pageable pageable) {
        return userService.getActiveUsers(pageable);
    }

    @GetMapping("/inactivos")
    public Page<UsuarioResponse> getInactiveUsers(Pageable pageable) {
        return userService.getInactiveUsers(pageable);
    }

    @GetMapping("/{id}")
    public UsuarioResponse getUserById(@PathVariable Long id) {
        return UsuarioResponse.from(userService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> createUser(
            @Validated(UsuarioRequest.OnCreate.class) @RequestBody UsuarioRequest user
    ) {
        UsuarioResponse response = UsuarioResponse.from(userService.createUser(user));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> updateUser(
            @PathVariable Long id,
            @Validated(UsuarioRequest.OnUpdate.class) @RequestBody UsuarioRequest user
    ) {
        return ResponseEntity.ok(UsuarioResponse.from(userService.updateUser(id, user)));
    }

    @PatchMapping("/desactivar/{id}")
    public ResponseEntity<Void> desactivarUsuario(@PathVariable Long id) {
        userService.desactivarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/activar/{id}")
    public ResponseEntity<UsuarioResponse> activarUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(UsuarioResponse.from(userService.activarUsuario(id)));
    }

    @GetMapping("/tipos-documento")
    public TipoDocumento[] listarTiposDocumento() {
        return TipoDocumento.values();
    }

    @GetMapping("/search")
    public Page<UsuarioResponse> searchUsuarios(
        Pageable pageable,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Boolean estado,
        @RequestParam(required = false) TipoDocumento tipoDocumento
    ) {
        return userService.searchUsuarios(
        pageable, search, estado, tipoDocumento
        );
    }
}