package com.sistemaposjcs.sistemaposjcs.modules.autenticacion.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.repository.RevokedJwtRepository;
import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.seguridad.JwtUtil;
import io.jsonwebtoken.Claims;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.Test;

class JwtTokenRevocationServiceTest {

    @Test
    void revokeLimpiaRegistrosExpiradosYRegistraElToken() {
        RevokedJwtRepository repository = mock(RevokedJwtRepository.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        Claims claims = mock(Claims.class);
        when(jwtUtil.extractClaims("jwt")).thenReturn(claims);
        when(claims.getId()).thenReturn("token-id");
        when(claims.getExpiration()).thenReturn(Date.from(Instant.now().plusSeconds(600)));
        when(repository.existsById("token-id")).thenReturn(false);

        new JwtTokenRevocationService(repository, jwtUtil).revoke("jwt");

        verify(repository).deleteByExpiraEnBefore(any(Instant.class));
        verify(repository).save(any());
    }

    @Test
    void revokeNoDuplicaTokenYaRevocado() {
        RevokedJwtRepository repository = mock(RevokedJwtRepository.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        Claims claims = mock(Claims.class);
        when(jwtUtil.extractClaims("jwt")).thenReturn(claims);
        when(claims.getId()).thenReturn("token-id");
        when(claims.getExpiration()).thenReturn(Date.from(Instant.now().plusSeconds(600)));
        when(repository.existsById("token-id")).thenReturn(true);

        new JwtTokenRevocationService(repository, jwtUtil).revoke("jwt");

        verify(repository, never()).save(any());
    }
}
