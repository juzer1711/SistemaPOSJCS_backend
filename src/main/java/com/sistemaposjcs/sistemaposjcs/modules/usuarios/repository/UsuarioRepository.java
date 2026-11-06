package com.sistemaposjcs.sistemaposjcs.modules.usuarios.repository;

import com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Consultas de persistencia del módulo Usuario. */
public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.idUsuario = :id")
    Optional<Usuario> findByIdForUpdate(@Param("id") Long id);

    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByDocumento(String documento);
    boolean existsByEmail(String email);
    boolean existsByUsernameAndIdUsuarioNot(String username, Long idUsuario);
    boolean existsByDocumentoAndIdUsuarioNot(String documento, Long idUsuario);
    boolean existsByEmailAndIdUsuarioNot(String email, Long idUsuario);
    Page<Usuario> findByEstadoTrue(Pageable pageable);
    Page<Usuario> findByEstadoFalse(Pageable pageable);
}
