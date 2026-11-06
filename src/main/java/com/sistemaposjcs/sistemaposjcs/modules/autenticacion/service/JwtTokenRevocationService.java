package com.sistemaposjcs.sistemaposjcs.modules.autenticacion.service;

import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.model.RevokedJwt;
import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.repository.RevokedJwtRepository;
import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.seguridad.JwtUtil;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JwtTokenRevocationService {

    private final RevokedJwtRepository revokedJwtRepository;
    private final JwtUtil jwtUtil;

    public JwtTokenRevocationService(RevokedJwtRepository revokedJwtRepository, JwtUtil jwtUtil) {
        this.revokedJwtRepository = revokedJwtRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional(readOnly = true)
    public boolean isRevoked(String tokenId) {
        return revokedJwtRepository.existsById(tokenId);
    }

    @Transactional
    public void revoke(String token) {
        var claims = jwtUtil.extractClaims(token);
        String tokenId = claims.getId();
        Instant expiresAt = claims.getExpiration().toInstant();

        revokedJwtRepository.deleteByExpiraEnBefore(Instant.now());
        if (!revokedJwtRepository.existsById(tokenId)) {
            revokedJwtRepository.save(new RevokedJwt(tokenId, expiresAt));
        }
    }
}
