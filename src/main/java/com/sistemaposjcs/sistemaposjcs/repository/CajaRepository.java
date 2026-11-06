package com.sistemaposjcs.sistemaposjcs.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.sistemaposjcs.sistemaposjcs.model.Caja;
import com.sistemaposjcs.sistemaposjcs.model.Enum.EstadoCaja;

public interface CajaRepository extends JpaRepository<Caja, Long>, JpaSpecificationExecutor<Caja> {


    Page<Caja> findByEstadoCaja(Pageable Pageable, EstadoCaja estadoCaja);

    List<Caja> findByEstadoCaja(EstadoCaja estadoCaja);

    List<Caja> findByUsuarioIdUsuario(Long idUsuario);

    Optional<Caja> findByEstadoCajaAndUsuarioIdUsuario(EstadoCaja estadoCaja, Long idUsuario);

    boolean existsByUsuarioIdUsuarioAndEstadoCaja(Long idUsuario, EstadoCaja estadoCaja);

    long countByEstadoCaja(EstadoCaja estadoCaja);

    List<Caja> findTop5ByEstadoCajaOrderByFechaAperturaDesc(EstadoCaja estadoCaja);

    Optional<Caja> findTopByEstadoCajaOrderByFechaCierreDesc(EstadoCaja estadoCaja);

}
