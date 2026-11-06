package com.sistemaposjcs.sistemaposjcs.modules.autenticacion.repository;

import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.model.RevokedJwt;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedJwtRepository extends JpaRepository<RevokedJwt, String> {

    void deleteByExpiraEnBefore(Instant instant);
}
