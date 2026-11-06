package com.sistemaposjcs.sistemaposjcs.modules.autenticacion.service;

import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.dto.LoginRequest;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario autenticar(LoginRequest request) {
        return usuarioRepository.findByUsername(request.getUsername())
                .filter(usuario -> Boolean.TRUE.equals(usuario.getEstado()))
                .filter(usuario -> passwordEncoder.matches(request.getPassword(), usuario.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario o contraseña incorrectos"
                ));
    }
}
