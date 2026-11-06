package com.sistemaposjcs.sistemaposjcs.modules.autenticacion.controller;

import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.dto.LoginRequest;
import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.dto.LoginResponse;
import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.seguridad.JwtUtil;
import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.service.AuthService;
import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.service.JwtTokenRevocationService;
import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.service.UsuarioAuthService;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final JwtTokenRevocationService tokenRevocationService;
    private final UsuarioAuthService usuarioAuthService;

    public AuthController(
            AuthService authService,
            JwtUtil jwtUtil,
            JwtTokenRevocationService tokenRevocationService,
            UsuarioAuthService usuarioAuthService
    ) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.tokenRevocationService = tokenRevocationService;
        this.usuarioAuthService = usuarioAuthService;
    }

    @PostMapping("/login")
    public LoginResponse iniciarSesion(@Valid @RequestBody LoginRequest request) {
        Usuario usuario = authService.autenticar(request);
        String token = jwtUtil.generateToken(usuario.getUsername());
        return new LoginResponse(
                "ok",
                usuario.getIdUsuario(),
                usuario.getUsername(),
                "Login exitoso",
                token
        );
    }

    @GetMapping("/me")
    public LoginResponse obtenerSesion() {
        Usuario usuario = usuarioAuthService.getUsuarioLogueado();
        return new LoginResponse("ok", usuario.getIdUsuario(), usuario.getUsername(), "Sesión activa", null);
    }

    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> cerrarSesion(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        String token = authorization.substring("Bearer ".length()).trim();
        tokenRevocationService.revoke(token);
        return ResponseEntity.ok(new LoginResponse(
                "ok",
                null,
                null,
                "Sesión cerrada; el token fue revocado",
                null
        ));
    }
}
