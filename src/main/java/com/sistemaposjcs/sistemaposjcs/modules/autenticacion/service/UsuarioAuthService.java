package com.sistemaposjcs.sistemaposjcs.modules.autenticacion.service;

import com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioAuthService {

    private final UsuarioRepository userRepository;

    public UsuarioAuthService(UsuarioRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Usuario getUsuarioLogueado() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth instanceof AnonymousAuthenticationToken || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No hay usuario autenticado");
        }

        String username = auth.getName();

        return userRepository.findByUsername(username)
                .filter(usuario -> Boolean.TRUE.equals(usuario.getEstado()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));
    }
}
