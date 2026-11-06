package com.sistemaposjcs.sistemaposjcs.service;

import org.springframework.stereotype.Service;

import com.sistemaposjcs.sistemaposjcs.dto.ReporteResumenDTO;
import com.sistemaposjcs.sistemaposjcs.repository.VentaRepository;
import com.sistemaposjcs.sistemaposjcs.dto.ReporteCajeroDTO;
import com.sistemaposjcs.sistemaposjcs.dto.ReporteMetodoPagoDTO;
import com.sistemaposjcs.sistemaposjcs.dto.ReporteProductoDTO;
import com.sistemaposjcs.sistemaposjcs.dto.ReporteStockBajoDTO;
import com.sistemaposjcs.sistemaposjcs.model.Caja;
import com.sistemaposjcs.sistemaposjcs.model.Cliente;
import com.sistemaposjcs.sistemaposjcs.modules.empresa.model.Empresa;
import com.sistemaposjcs.sistemaposjcs.model.Producto;
import com.sistemaposjcs.sistemaposjcs.model.Venta;
import com.sistemaposjcs.sistemaposjcs.model.Enum.EstadoCaja;
import com.sistemaposjcs.sistemaposjcs.repository.CajaRepository;
import com.sistemaposjcs.sistemaposjcs.repository.ClienteRepository;
import com.sistemaposjcs.sistemaposjcs.modules.empresa.repository.EmpresaRepository;
import com.sistemaposjcs.sistemaposjcs.repository.MovimientoInventarioRepository;
import com.sistemaposjcs.sistemaposjcs.repository.ProductoRepository;


import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
public class ReporteService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final ClienteRepository clienteRepository;
    private final CajaRepository cajaRepository;
    private final EmpresaRepository empresaRepository;

    public ReporteService(
            VentaRepository ventaRepository,
            ProductoRepository productoRepository,
            MovimientoInventarioRepository movimientoInventarioRepository,
            ClienteRepository clienteRepository,
            CajaRepository cajaRepository,
            EmpresaRepository empresaRepository
    ) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.clienteRepository = clienteRepository;
        this.cajaRepository = cajaRepository;
        this.empresaRepository = empresaRepository;
    }

    public ReporteResumenDTO obtenerResumen() {

        

        Long ventasHoy = obtenerCantidadVentasHoy();

        Long ventasMes = obtenerCantidadVentasMes();

        BigDecimal totalFacturado = ventaRepository.sumTotalVentas();

        BigDecimal ivaRecaudado = ventaRepository.sumTotalIVA();

        return new ReporteResumenDTO(
                ventasHoy,
                ventasMes,
                totalFacturado,
                ivaRecaudado
        );
    }

    public Long obtenerCantidadVentasHoy() {

        return contarVentasActivasEntre(
                inicioDelDiaActual(),
                finDelDiaActual()
        );
    }

    public Long obtenerCantidadVentasMes() {

        return contarVentasActivasEntre(
                inicioDelMesActual(),
                finDelDiaActual()
        );
    }

    public BigDecimal obtenerTotalVentasHoy() {
        return obtenerTotalVentasEntre(
                inicioDelDiaActual(),
                finDelDiaActual()
        );
    }

    public BigDecimal obtenerTotalVentasSemana() {
        return obtenerTotalVentasEntre(
                inicioDeSemanaActual(),
                finDelDiaActual()
        );
    }

    public BigDecimal obtenerTotalVentasMes() {
        return obtenerTotalVentasEntre(
                inicioDelMesActual(),
                finDelDiaActual()
        );
    }

    public List<Venta> obtenerUltimasVentas() {
        return ventaRepository.findTop5ByEstadoTrueOrderByFechaDesc();
    }

    public Optional<Venta> obtenerVentaMasAltaDia() {
        return obtenerVentaMasAltaEntre(
                inicioDelDiaActual(),
                finDelDiaActual()
        );
    }

    public Optional<Venta> obtenerVentaMasAltaMes() {
        return obtenerVentaMasAltaEntre(
                inicioDelMesActual(),
                finDelDiaActual()
        );
    }

    private Long contarVentasActivasEntre(
            LocalDateTime inicio,
            LocalDateTime fin
    ) {
        return ventaRepository.countByEstadoTrueAndFechaBetween(
                inicio,
                fin
        );
    }

    private BigDecimal obtenerTotalVentasEntre(
            LocalDateTime inicio,
            LocalDateTime fin
    ) {
        return ventaRepository.sumTotalVentasBetween(
                inicio,
                fin
        );
    }

    private Optional<Venta> obtenerVentaMasAltaEntre(
            LocalDateTime inicio,
            LocalDateTime fin
    ) {
        return ventaRepository.findTopByEstadoTrueAndFechaBetweenOrderByTotalDesc(
                inicio,
                fin
        );
    }

    private LocalDateTime inicioDelDiaActual() {
        return LocalDate.now().atStartOfDay();
    }

    private LocalDateTime finDelDiaActual() {
        return LocalDate.now().atTime(23, 59, 59);
    }

    private LocalDateTime inicioDeSemanaActual() {
        return LocalDate.now()
                .with(DayOfWeek.MONDAY)
                .atStartOfDay();
    }

    private LocalDateTime inicioDelMesActual() {
        return LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay();
    }

    public List<ReporteMetodoPagoDTO> obtenerVentasPorMetodoPago() {

        List<Object[]> resultados = ventaRepository.obtenerVentasPorMetodoPago();

        return resultados.stream()
                .map(r -> new ReporteMetodoPagoDTO(
                        r[0].toString(),
                        (BigDecimal) r[1]
                ))
                .toList();
    }

    public BigDecimal obtenerIngresosPorMetodoPago(String metodoPago) {
        return ventaRepository.sumTotalVentasByMetodoPago(metodoPago);
    }

    public Optional<ReporteMetodoPagoDTO> obtenerMetodoPagoMasUsado() {
        return ventaRepository.obtenerMetodoPagoMasUsado()
                .stream()
                .findFirst()
                .map(r -> new ReporteMetodoPagoDTO(
                        r[0].toString(),
                        BigDecimal.valueOf(((Number) r[1]).longValue())
                ));
    }

    public List<ReporteProductoDTO> obtenerTopProductosVendidos() {

        List<Object[]> resultados =
                ventaRepository.obtenerTopProductosVendidos();

        return convertirReporteProductos(resultados);
    }

    public Optional<ReporteProductoDTO> obtenerProductoMasVendido() {
        return obtenerTopProductosVendidos()
                .stream()
                .findFirst();
    }

    public Optional<ReporteProductoDTO> obtenerProductoMenosVendido() {
        List<Object[]> resultados =
                ventaRepository.obtenerProductosMenosVendidos();

        return convertirReporteProductos(resultados)
                .stream()
                .findFirst();
    }

    public Long obtenerTotalProductos() {
        return productoRepository.count();
    }

    public Long obtenerTotalProductosActivos() {
        return productoRepository.countByEstadoTrue();
    }

    public Long obtenerTotalProductosInactivos() {
        return productoRepository.countByEstadoFalse();
    }

    private List<ReporteProductoDTO> convertirReporteProductos(
            List<Object[]> resultados
    ) {
        return resultados.stream()
                .map(r -> new ReporteProductoDTO(
                        r[0].toString(),
                        ((Number) r[1]).longValue()
                ))
                .toList();
    }

    public List<ReporteCajeroDTO> obtenerVentasPorCajero() {

        List<Object[]> resultados =
                ventaRepository.obtenerVentasPorCajero();

        return convertirReporteCajeros(resultados);
    }

    public Optional<ReporteCajeroDTO> obtenerCajeroMasVentasHoy() {
        return obtenerVentasPorCajeroEntre(
                inicioDelDiaActual(),
                finDelDiaActual()
        ).stream().findFirst();
    }

    public Optional<ReporteCajeroDTO> obtenerCajeroMasVentasMes() {
        return obtenerVentasPorCajeroEntre(
                inicioDelMesActual(),
                finDelDiaActual()
        ).stream().findFirst();
    }

    public List<ReporteCajeroDTO> obtenerRankingCajeros() {
        return obtenerVentasPorCajero();
    }

    private List<ReporteCajeroDTO> obtenerVentasPorCajeroEntre(
            LocalDateTime inicio,
            LocalDateTime fin
    ) {
        List<Object[]> resultados =
                ventaRepository.obtenerVentasPorCajeroEntre(inicio, fin);

        return convertirReporteCajeros(resultados);
    }

    private List<ReporteCajeroDTO> convertirReporteCajeros(
            List<Object[]> resultados
    ) {
        return resultados.stream()
                .map(r -> new ReporteCajeroDTO(
                        r[0].toString(),
                        (BigDecimal) r[1]
                ))
                .toList();
    }

    public List<ReporteStockBajoDTO> obtenerStock(String tipo) {

        List<Producto> productos = productoRepository.findAll();

        return productos.stream()
                .filter(p -> {

                    if (tipo == null) {
                        return p.getStockActual() <= p.getStockMinimo();
                    }

                    return switch (tipo) {
                        case "critico" -> p.getStockActual() <= 0;

                        case "bajo" -> p.getStockActual() > 0
                                && p.getStockActual() <= p.getStockMinimo();

                        case "normal" -> p.getStockMinimo() > 0
                                && p.getStockActual() > p.getStockMinimo();

                        default -> true;
                    };
                })
                .map(this::convertirStockProducto)
                .toList();
    }

    public List<ReporteStockBajoDTO> obtenerProductosStockBajo() {
        return obtenerStock(null);
    }

    public List<ReporteStockBajoDTO> obtenerProductosAgotados() {
        return productoRepository.findProductosAgotados()
                .stream()
                .map(this::convertirStockProducto)
                .toList();
    }

    public Long obtenerCantidadMovimientosInventarioHoy() {
        return movimientoInventarioRepository.countByFechaBetween(
                inicioDelDiaActual(),
                finDelDiaActual()
        );
    }

    public Optional<Producto> obtenerProductoMayorStock() {
        return productoRepository.findTopByEstadoTrueOrderByStockActualDesc();
    }

    public Optional<Producto> buscarProductoParaStock(String busqueda) {
        if (busqueda == null || busqueda.isBlank()) {
            return Optional.empty();
        }

        return productoRepository.findFirstByEstadoTrueAndNombreContainingIgnoreCaseOrderByNombreAsc(
                busqueda.trim()
        );
    }

    private ReporteStockBajoDTO convertirStockProducto(Producto producto) {
        return new ReporteStockBajoDTO(
                producto.getNombre(),
                producto.getStockActual(),
                producto.getStockMinimo()
        );
    }

    public Long obtenerTotalClientes() {
        return clienteRepository.count();
    }

    public Long obtenerTotalClientesActivos() {
        return clienteRepository.countByEstadoTrue();
    }

    public List<Cliente> obtenerUltimosClientes() {
        return clienteRepository.findTop5ByEstadoTrueOrderByIdClienteDesc();
    }

    public Long obtenerTotalCajasAbiertas() {
        return cajaRepository.countByEstadoCaja(EstadoCaja.ABIERTA);
    }

    public List<Caja> obtenerCajasAbiertas() {
        return cajaRepository.findTop5ByEstadoCajaOrderByFechaAperturaDesc(
                EstadoCaja.ABIERTA
        );
    }

    public Optional<Caja> obtenerCajaPrincipal() {
        return obtenerTodasCajasAbiertas()
                .stream()
                .max((c1, c2) -> c1.getTotalVentas().compareTo(c2.getTotalVentas()));
    }

    public Optional<Caja> obtenerUltimoCierreCaja() {
        return cajaRepository.findTopByEstadoCajaOrderByFechaCierreDesc(
                EstadoCaja.CERRADA
        );
    }

    public BigDecimal obtenerDineroActualCajasAbiertas() {
        return obtenerTodasCajasAbiertas()
                .stream()
                .map(caja -> caja.getMontoInicial().add(caja.getTotalVentas()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<Caja> obtenerTodasCajasAbiertas() {
        return cajaRepository.findByEstadoCaja(EstadoCaja.ABIERTA);
    }

    public Optional<Empresa> obtenerEmpresaActiva() {
        return empresaRepository.findFirstByOrderByIdEmpresaAsc();
    }
}
