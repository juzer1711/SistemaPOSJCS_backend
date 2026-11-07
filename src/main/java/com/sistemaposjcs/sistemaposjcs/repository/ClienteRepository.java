package com.sistemaposjcs.sistemaposjcs.repository;

import com.sistemaposjcs.sistemaposjcs.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long>, JpaSpecificationExecutor<Cliente> {

    Page<Cliente> findByEstadoTrue(Pageable pageable);
    Page<Cliente> findByEstadoFalse(Pageable pageable);

    long countByEstadoTrue();

    List<Cliente> findTop5ByEstadoTrueOrderByIdClienteDesc();

}
