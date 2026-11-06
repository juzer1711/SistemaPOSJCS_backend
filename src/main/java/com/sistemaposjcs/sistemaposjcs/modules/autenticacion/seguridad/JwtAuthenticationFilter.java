package com.sistemaposjcs.sistemaposjcs.modules.autenticacion.seguridad;

import java.io.IOException;
import java.util.List;

import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.service.JwtTokenRevocationService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.repository.UsuarioRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;
    private final JwtTokenRevocationService tokenRevocationService;

    public JwtAuthenticationFilter(
            JwtUtil jwtUtil,
            UsuarioRepository usuarioRepository,
            JwtTokenRevocationService tokenRevocationService
    ) {
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
        this.tokenRevocationService = tokenRevocationService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");
        String token = authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring("Bearer ".length()).trim()
                : null;

        if (token != null && !token.isBlank() && jwtUtil.isTokenValid(token)
                && !tokenRevocationService.isRevoked(jwtUtil.extractTokenId(token))) {
            String username = jwtUtil.extractUsername(token);
            usuarioRepository.findByUsername(username)
                    .filter(usuario -> Boolean.TRUE.equals(usuario.getEstado()))
                    .ifPresent(usuario -> {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        usuario.getUsername(),
                                        null,
                                        List.of()
                                );
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    });
        }

        filterChain.doFilter(request, response);
    }
}
