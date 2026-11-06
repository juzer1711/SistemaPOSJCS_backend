package com.sistemaposjcs.sistemaposjcs.modules.autenticacion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "jwt_revocados",
        indexes = @Index(name = "idx_jwt_revocados_expiracion", columnList = "expira_en")
)
@Getter
@Setter
@NoArgsConstructor
public class RevokedJwt {

    @Id
    @Column(name = "id_token", nullable = false, length = 64)
    private String idToken;

    @Column(name = "expira_en", nullable = false)
    private Instant expiraEn;

    public RevokedJwt(String idToken, Instant expiraEn) {
        this.idToken = idToken;
        this.expiraEn = expiraEn;
    }
}
