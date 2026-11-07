package com.sistemaposjcs.sistemaposjcs.modules.empresa.repository;

import com.sistemaposjcs.sistemaposjcs.modules.empresa.model.Empresa;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findFirstByOrderByIdEmpresaAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Empresa e where e.idEmpresa = :id")
    Optional<Empresa> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Empresa> findFirstWithLockByOrderByIdEmpresaAsc();
}
