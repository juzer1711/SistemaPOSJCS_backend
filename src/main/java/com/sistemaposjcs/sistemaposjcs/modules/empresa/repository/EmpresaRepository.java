package com.sistemaposjcs.sistemaposjcs.modules.empresa.repository;

import com.sistemaposjcs.sistemaposjcs.modules.empresa.model.Empresa;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findFirstByOrderByIdEmpresaAsc();
}
