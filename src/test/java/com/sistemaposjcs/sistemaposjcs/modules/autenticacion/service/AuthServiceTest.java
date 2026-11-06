package com.sistemaposjcs.sistemaposjcs.modules.autenticacion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.dto.LoginRequest;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void autenticarRechazaUsuarioInexistente() {
        when(usuarioRepository.findByUsername("desconocido")).thenReturn(Optional.empty());

        assertUnauthorized(login("desconocido", "secreto"));
    }

    @Test
    void autenticarRechazaContrasenaIncorrecta() {
        Usuario usuario = usuarioActivo();
        when(usuarioRepository.findByUsername("prueba")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("incorrecta", usuario.getPassword())).thenReturn(false);

        assertUnauthorized(login("prueba", "incorrecta"));
    }

    @Test
    void autenticarRechazaUsuarioInactivo() {
        Usuario usuario = usuarioActivo();
        usuario.setEstado(false);
        when(usuarioRepository.findByUsername("prueba")).thenReturn(Optional.of(usuario));

        assertUnauthorized(login("prueba", "correcta"));
    }

    private void assertUnauthorized(LoginRequest request) {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.autenticar(request)
        );
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Usuario o contraseña incorrectos", exception.getReason());
    }

    private LoginRequest login(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    private Usuario usuarioActivo() {
        Usuario usuario = new Usuario();
        usuario.setUsername("prueba");
        usuario.setPassword("$2a$10$hash");
        usuario.setEstado(true);
        return usuario;
    }
}
