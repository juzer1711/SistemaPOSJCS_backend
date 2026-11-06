package com.sistemaposjcs.sistemaposjcs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.sistemaposjcs.sistemaposjcs.model.Venta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, Long>, JpaSpecificationExecutor<Venta> {

    // ventas activas
    Page<Venta> findByEstadoTrue(Pageable pageable);
    // ventas inactivas
    Page<Venta> findByEstadoFalse(Pageable pageable);

    List<Venta> findByCajaIdCaja(Long idCaja);

    @Query("""
        SELECT v.metodoPago, COALESCE(SUM(v.total), 0)
        FROM Venta v
        WHERE v.estado = true
        GROUP BY v.metodoPago
    """)
    List<Object[]> obtenerVentasPorMetodoPago();

    @Query(value = """
        SELECT COALESCE(SUM(total), 0)
        FROM ventas
        WHERE estado = true
        AND metodo_pago = :metodoPago
    """, nativeQuery = true)
    BigDecimal sumTotalVentasByMetodoPago(
            @Param("metodoPago") String metodoPago
    );

    @Query(value = """
        SELECT metodo_pago, COUNT(*)
        FROM ventas
        WHERE estado = true
        GROUP BY metodo_pago
        ORDER BY COUNT(*) DESC
        LIMIT 1
    """, nativeQuery = true)
    List<Object[]> obtenerMetodoPagoMasUsado();

    long countByEstadoTrue();

    long countByEstadoTrueAndFechaBetween(
            LocalDateTime inicio,
            LocalDateTime fin
    );

    @Query("""
        SELECT COALESCE(SUM(v.total), 0)
        FROM Venta v
        WHERE v.estado = true
        AND v.fecha BETWEEN :inicio AND :fin
    """)
    BigDecimal sumTotalVentasBetween(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    List<Venta> findTop5ByEstadoTrueOrderByFechaDesc();

    Optional<Venta> findTopByEstadoTrueAndFechaBetweenOrderByTotalDesc(
            LocalDateTime inicio,
            LocalDateTime fin
    );

    @org.springframework.data.jpa.repository.Query("""
        SELECT COALESCE(SUM(v.total),0)
        FROM Venta v
        WHERE v.estado = true
    """)
    java.math.BigDecimal sumTotalVentas();

    @org.springframework.data.jpa.repository.Query("""
        SELECT COALESCE(SUM(v.totalIVA),0)
        FROM Venta v
        WHERE v.estado = true
    """)
    java.math.BigDecimal sumTotalIVA();

    @Query("""
        SELECT
            i.producto.nombre,
            SUM(i.cantidad)
        FROM ItemFactura i
        JOIN i.venta v
        WHERE v.estado = true
        GROUP BY i.producto.nombre
        ORDER BY SUM(i.cantidad) DESC
    """)
    List<Object[]> obtenerTopProductosVendidos();

    @Query("""
        SELECT
            i.producto.nombre,
            SUM(i.cantidad)
        FROM ItemFactura i
        JOIN i.venta v
        WHERE v.estado = true
        GROUP BY i.producto.nombre
        ORDER BY SUM(i.cantidad) ASC
    """)
    List<Object[]> obtenerProductosMenosVendidos();

    @Query("""
        SELECT
            CONCAT(u.primerNombre, ' ', u.primerApellido),
            COALESCE(SUM(v.total),0)
        FROM Venta v
        JOIN v.usuario u
        WHERE v.estado = true
        GROUP BY
            u.idUsuario,
            u.primerNombre,
            u.primerApellido
        ORDER BY COALESCE(SUM(v.total),0) DESC
    """)
    List<Object[]> obtenerVentasPorCajero();

    @Query("""
        SELECT
            CONCAT(u.primerNombre, ' ', u.primerApellido),
            COALESCE(SUM(v.total),0)
        FROM Venta v
        JOIN v.usuario u
        WHERE v.estado = true
        AND v.fecha BETWEEN :inicio AND :fin
        GROUP BY
            u.idUsuario,
            u.primerNombre,
            u.primerApellido
        ORDER BY COALESCE(SUM(v.total),0) DESC
    """)
    List<Object[]> obtenerVentasPorCajeroEntre(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

}
