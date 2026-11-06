package com.sistemaposjcs.sistemaposjcs.repository;

import com.sistemaposjcs.sistemaposjcs.model.Producto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository

public interface ProductoRepository extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {
    Page<Producto> findByEstadoTrue(Pageable pageable);
    Page<Producto> findByEstadoFalse(Pageable pageable);

    long countByEstadoTrue();
    long countByEstadoFalse();

    @Query("""
        SELECT p
        FROM Producto p
        WHERE p.stockMinimo > 0
        AND p.stockActual <= p.stockMinimo
    """)
    List<Producto> findProductosBajoStock();

    @Query("""
        SELECT p
        FROM Producto p
        WHERE p.stockActual <= 0
    """)
    List<Producto> findProductosAgotados();

    Optional<Producto> findTopByEstadoTrueOrderByStockActualDesc();

    Optional<Producto> findFirstByEstadoTrueAndNombreContainingIgnoreCaseOrderByNombreAsc(
            String nombre
    );
    
}
