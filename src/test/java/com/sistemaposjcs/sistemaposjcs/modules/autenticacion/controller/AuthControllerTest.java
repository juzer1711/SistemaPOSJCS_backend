package com.sistemaposjcs.sistemaposjcs.modules.autenticacion.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.dto.LoginRequest;
import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.dto.LoginResponse;
import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.seguridad.JwtUtil;
import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.service.AuthService;
import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.service.JwtTokenRevocationService;
import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.service.UsuarioAuthService;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class AuthControllerTest {

    @Test
    void loginDevuelveJwtEnRespuesta() {
        AuthService authService = mock(AuthService.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        LoginRequest request = new LoginRequest();
        request.setUsername("prueba");
        request.setPassword("correcta");
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(7L);
        usuario.setUsername("prueba");
        when(authService.autenticar(request)).thenReturn(usuario);
        when(jwtUtil.generateToken("prueba")).thenReturn("jwt-de-prueba");

        AuthController controller = new AuthController(
                authService,
                jwtUtil,
                mock(JwtTokenRevocationService.class),
                mock(UsuarioAuthService.class)
        );

        LoginResponse response = controller.iniciarSesion(request);

        assertEquals("jwt-de-prueba", response.getToken());
    }

    @Test
    void logoutRevocaElTokenEnviadoComoBearer() {
        JwtTokenRevocationService tokenRevocationService = mock(JwtTokenRevocationService.class);
        AuthController controller = new AuthController(
                mock(AuthService.class),
                mock(JwtUtil.class),
                tokenRevocationService,
                mock(UsuarioAuthService.class)
        );

        var response = controller.cerrarSesion("Bearer jwt-de-prueba");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(tokenRevocationService).revoke("jwt-de-prueba");
    }
}
