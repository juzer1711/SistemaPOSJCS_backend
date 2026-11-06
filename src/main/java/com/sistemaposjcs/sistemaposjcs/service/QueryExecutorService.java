package com.sistemaposjcs.sistemaposjcs.service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.sistemaposjcs.sistemaposjcs.dto.ReporteCajeroDTO;
import com.sistemaposjcs.sistemaposjcs.dto.ReporteMetodoPagoDTO;
import com.sistemaposjcs.sistemaposjcs.dto.ReporteProductoDTO;
import com.sistemaposjcs.sistemaposjcs.dto.ReporteStockBajoDTO;
import com.sistemaposjcs.sistemaposjcs.model.Caja;
import com.sistemaposjcs.sistemaposjcs.model.Cliente;
import com.sistemaposjcs.sistemaposjcs.modules.empresa.model.Empresa;
import com.sistemaposjcs.sistemaposjcs.model.Producto;
import com.sistemaposjcs.sistemaposjcs.model.Venta;
import com.sistemaposjcs.sistemaposjcs.model.Enum.TipoConsulta;

@Service
public class QueryExecutorService {

    private final ReporteService reporteService;
    private final NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(
            new Locale("es", "CO")
    );
    private final DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern(
            "dd/MM/yyyy HH:mm"
    );

    public QueryExecutorService(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    public String ejecutar(TipoConsulta tipoConsulta) {
        return ejecutar(tipoConsulta, null);
    }

    public String ejecutar(TipoConsulta tipoConsulta, String pregunta) {

        return switch (tipoConsulta) {
            case VENTAS_HOY -> responderVentasHoy();
            case VENTAS_SEMANA -> responderVentasSemana();
            case VENTAS_MES -> responderVentasMes();
            case CANTIDAD_VENTAS_HOY -> responderCantidadVentasHoy();
            case ULTIMAS_VENTAS -> responderUltimasVentas();
            case VENTA_MAS_ALTA_DIA -> responderVentaMasAltaDia();
            case VENTA_MAS_ALTA_MES -> responderVentaMasAltaMes();
            case PRODUCTO_MAS_VENDIDO -> responderProductoMasVendido();
            case TOP_PRODUCTOS_MAS_VENDIDOS -> responderTopProductosMasVendidos();
            case PRODUCTO_MENOS_VENDIDO -> responderProductoMenosVendido();
            case TOTAL_PRODUCTOS -> responderTotalProductos();
            case PRODUCTOS_ACTIVOS -> responderProductosActivos();
            case PRODUCTOS_INACTIVOS -> responderProductosInactivos();
            case PRODUCTOS_STOCK_BAJO -> responderProductosStockBajo();
            case PRODUCTOS_AGOTADOS -> responderProductosAgotados();
            case MOVIMIENTOS_INVENTARIO_HOY -> responderMovimientosInventarioHoy();
            case PRODUCTO_MAYOR_STOCK -> responderProductoMayorStock();
            case STOCK_PRODUCTO -> responderStockProducto(pregunta);
            case TOTAL_CLIENTES -> responderTotalClientes();
            case CLIENTES_ACTIVOS -> responderClientesActivos();
            case ULTIMOS_CLIENTES -> responderUltimosClientes();
            case TOTAL_CAJAS_ABIERTAS -> responderTotalCajasAbiertas();
            case LISTAR_CAJAS_ABIERTAS -> responderListarCajasAbiertas();
            case USUARIO_CAJA_PRINCIPAL -> responderUsuarioCajaPrincipal();
            case ULTIMO_CIERRE_CAJA -> responderUltimoCierreCaja();
            case DINERO_ACTUAL_CAJA -> responderDineroActualCaja();
            case CAJERO_MAS_VENTAS_MES -> responderCajeroMasVentasMes();
            case CAJERO_MAS_VENTAS_HOY -> responderCajeroMasVentasHoy();
            case VENTAS_POR_CAJERO -> responderVentasPorCajero();
            case RANKING_CAJEROS -> responderRankingCajeros();
            case INGRESOS_EFECTIVO -> responderIngresosMetodoPago("EFECTIVO", "efectivo");
            case INGRESOS_TRANSFERENCIA -> responderIngresosMetodoPago("TRANSFERENCIA", "transferencia");
            case METODO_PAGO_MAS_USADO -> responderMetodoPagoMasUsado();
            case NOMBRE_EMPRESA -> responderNombreEmpresa();
            case NIT_EMPRESA -> responderNitEmpresa();
            case TELEFONO_EMPRESA -> responderTelefonoEmpresa();
            default -> "No pude entender la consulta. Intenta preguntar, por ejemplo: cuantas ventas hay hoy.";
        };
    }

    private String responderVentasHoy() {
        BigDecimal totalVentasHoy = reporteService.obtenerTotalVentasHoy();

        return "Hoy se ha vendido un total de " + formatearMoneda(totalVentasHoy) + ".";
    }

    private String responderVentasSemana() {
        BigDecimal totalVentasSemana = reporteService.obtenerTotalVentasSemana();

        return "Esta semana se ha vendido un total de " + formatearMoneda(totalVentasSemana) + ".";
    }

    private String responderVentasMes() {
        BigDecimal totalVentasMes = reporteService.obtenerTotalVentasMes();

        return "Este mes se ha vendido un total de " + formatearMoneda(totalVentasMes) + ".";
    }

    private String responderCantidadVentasHoy() {
        Long ventasHoy = reporteService.obtenerCantidadVentasHoy();

        if (ventasHoy == 1) {
            return "Hoy se ha registrado 1 venta.";
        }

        return "Hoy se han registrado " + ventasHoy + " ventas.";
    }

    private String responderUltimasVentas() {
        List<Venta> ventas = reporteService.obtenerUltimasVentas();

        if (ventas.isEmpty()) {
            return "No hay ventas registradas.";
        }

        StringBuilder respuesta = new StringBuilder("Ultimas ventas registradas:");

        for (Venta venta : ventas) {
            respuesta.append("\n")
                    .append("- Venta #")
                    .append(venta.getIdVenta())
                    .append(" | ")
                    .append(formatearFecha(venta))
                    .append(" | Cliente: ")
                    .append(venta.getCliente().getNombreCompleto())
                    .append(" | Total: ")
                    .append(formatearMoneda(venta.getTotal()));
        }

        return respuesta.toString();
    }

    private String responderVentaMasAltaDia() {
        return reporteService.obtenerVentaMasAltaDia()
                .map(venta -> "La venta mas alta de hoy fue la venta #"
                        + venta.getIdVenta()
                        + " por "
                        + formatearMoneda(venta.getTotal())
                        + ", realizada a "
                        + venta.getCliente().getNombreCompleto()
                        + ".")
                .orElse("Hoy no hay ventas registradas.");
    }

    private String responderVentaMasAltaMes() {
        return reporteService.obtenerVentaMasAltaMes()
                .map(venta -> "La venta mas alta de este mes fue la venta #"
                        + venta.getIdVenta()
                        + " por "
                        + formatearMoneda(venta.getTotal())
                        + ", realizada a "
                        + venta.getCliente().getNombreCompleto()
                        + " el "
                        + formatearFecha(venta)
                        + ".")
                .orElse("Este mes no hay ventas registradas.");
    }

    private String responderProductoMasVendido() {
        return reporteService.obtenerProductoMasVendido()
                .map(producto -> "El producto mas vendido es "
                        + producto.getProducto()
                        + " con "
                        + producto.getCantidad()
                        + " unidades vendidas.")
                .orElse("No hay productos vendidos registrados.");
    }

    private String responderTopProductosMasVendidos() {
        List<ReporteProductoDTO> productos = reporteService.obtenerTopProductosVendidos();

        if (productos.isEmpty()) {
            return "No hay productos vendidos registrados.";
        }

        StringBuilder respuesta = new StringBuilder("Productos mas vendidos:");

        int limite = Math.min(productos.size(), 5);
        for (int i = 0; i < limite; i++) {
            ReporteProductoDTO producto = productos.get(i);

            respuesta.append("\n")
                    .append(i + 1)
                    .append(". ")
                    .append(producto.getProducto())
                    .append(" | ")
                    .append(producto.getCantidad())
                    .append(" unidades vendidas");
        }

        return respuesta.toString();
    }

    private String responderProductoMenosVendido() {
        return reporteService.obtenerProductoMenosVendido()
                .map(producto -> "El producto menos vendido es "
                        + producto.getProducto()
                        + " con "
                        + producto.getCantidad()
                        + " unidades vendidas.")
                .orElse("No hay productos vendidos registrados.");
    }

    private String responderTotalProductos() {
        Long totalProductos = reporteService.obtenerTotalProductos();

        if (totalProductos == 1) {
            return "Hay 1 producto registrado.";
        }

        return "Hay " + totalProductos + " productos registrados.";
    }

    private String responderProductosActivos() {
        Long totalProductosActivos = reporteService.obtenerTotalProductosActivos();

        if (totalProductosActivos == 1) {
            return "Hay 1 producto activo.";
        }

        return "Hay " + totalProductosActivos + " productos activos.";
    }

    private String responderProductosInactivos() {
        Long totalProductosInactivos = reporteService.obtenerTotalProductosInactivos();

        if (totalProductosInactivos == 1) {
            return "Hay 1 producto inactivo.";
        }

        return "Hay " + totalProductosInactivos + " productos inactivos.";
    }

    private String responderProductosStockBajo() {
        List<ReporteStockBajoDTO> productos = reporteService.obtenerProductosStockBajo();

        if (productos.isEmpty()) {
            return "No hay productos con stock bajo.";
        }

        return formatearProductosConStock(
                "Productos con stock bajo:",
                productos
        );
    }

    private String responderProductosAgotados() {
        List<ReporteStockBajoDTO> productos = reporteService.obtenerProductosAgotados();

        if (productos.isEmpty()) {
            return "No hay productos agotados.";
        }

        return formatearProductosConStock(
                "Productos agotados:",
                productos
        );
    }

    private String responderMovimientosInventarioHoy() {
        Long movimientosHoy = reporteService.obtenerCantidadMovimientosInventarioHoy();

        if (movimientosHoy == 1) {
            return "Hoy se ha registrado 1 movimiento de inventario.";
        }

        return "Hoy se han registrado " + movimientosHoy + " movimientos de inventario.";
    }

    private String responderProductoMayorStock() {
        return reporteService.obtenerProductoMayorStock()
                .map(producto -> "El producto con mayor stock es "
                        + producto.getNombre()
                        + " con "
                        + producto.getStockActual()
                        + " unidades disponibles.")
                .orElse("No hay productos activos registrados.");
    }

    private String responderStockProducto(String pregunta) {
        String busqueda = extraerBusquedaStockProducto(pregunta);

        if (busqueda.isBlank()) {
            return "Indica el nombre del producto para consultar su stock.";
        }

        return reporteService.buscarProductoParaStock(busqueda)
                .map(this::formatearStockProducto)
                .orElse("No encontre un producto activo que coincida con: " + busqueda + ".");
    }

    private String formatearProductosConStock(
            String titulo,
            List<ReporteStockBajoDTO> productos
    ) {
        StringBuilder respuesta = new StringBuilder(titulo);

        int limite = Math.min(productos.size(), 5);
        for (int i = 0; i < limite; i++) {
            ReporteStockBajoDTO producto = productos.get(i);

            respuesta.append("\n")
                    .append(i + 1)
                    .append(". ")
                    .append(producto.getProducto())
                    .append(" | Stock: ")
                    .append(producto.getStockActual())
                    .append(" | Minimo: ")
                    .append(producto.getStockMinimo());
        }

        if (productos.size() > limite) {
            respuesta.append("\n")
                    .append("Y ")
                    .append(productos.size() - limite)
                    .append(" productos mas.");
        }

        return respuesta.toString();
    }

    private String formatearStockProducto(Producto producto) {
        return "El producto "
                + producto.getNombre()
                + " tiene "
                + producto.getStockActual()
                + " unidades disponibles. Stock minimo: "
                + producto.getStockMinimo()
                + ".";
    }

    private String extraerBusquedaStockProducto(String pregunta) {
        if (pregunta == null) {
            return "";
        }

        String texto = normalizarTexto(pregunta)
                .replace("cuanto stock tiene", "")
                .replace("cuantos stock tiene", "")
                .replace("cuantas unidades tiene", "")
                .replace("cuantas existencias tiene", "")
                .replace("stock del producto", "")
                .replace("stock de producto", "")
                .replace("stock del", "")
                .replace("stock de", "")
                .replace("inventario del producto", "")
                .replace("inventario de producto", "")
                .replace("inventario del", "")
                .replace("inventario de", "")
                .replace("producto", "")
                .replace("productos", "")
                .replace("?", "")
                .trim();

        return texto.replaceAll("\\s+", " ");
    }

    private String normalizarTexto(String texto) {
        String sinAcentos = Normalizer
                .normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return sinAcentos.toLowerCase(Locale.ROOT).trim();
    }

    private String responderTotalClientes() {
        Long totalClientes = reporteService.obtenerTotalClientes();

        if (totalClientes == 1) {
            return "Hay 1 cliente registrado.";
        }

        return "Hay " + totalClientes + " clientes registrados.";
    }

    private String responderClientesActivos() {
        Long clientesActivos = reporteService.obtenerTotalClientesActivos();

        if (clientesActivos == 1) {
            return "Hay 1 cliente activo.";
        }

        return "Hay " + clientesActivos + " clientes activos.";
    }

    private String responderUltimosClientes() {
        List<Cliente> clientes = reporteService.obtenerUltimosClientes();

        if (clientes.isEmpty()) {
            return "No hay clientes activos registrados.";
        }

        StringBuilder respuesta = new StringBuilder("Ultimos clientes registrados:");

        for (Cliente cliente : clientes) {
            respuesta.append("\n")
                    .append("- Cliente #")
                    .append(cliente.getIdCliente())
                    .append(" | ")
                    .append(cliente.getNombreCompleto())
                    .append(" | Documento: ")
                    .append(cliente.getDocumento());
        }

        return respuesta.toString();
    }

    private String responderTotalCajasAbiertas() {
        Long cajasAbiertas = reporteService.obtenerTotalCajasAbiertas();

        if (cajasAbiertas == 1) {
            return "Hay 1 caja abierta.";
        }

        return "Hay " + cajasAbiertas + " cajas abiertas.";
    }

    private String responderListarCajasAbiertas() {
        List<Caja> cajas = reporteService.obtenerCajasAbiertas();

        if (cajas.isEmpty()) {
            return "No hay cajas abiertas.";
        }

        StringBuilder respuesta = new StringBuilder("Cajas abiertas:");

        for (Caja caja : cajas) {
            respuesta.append("\n")
                    .append("- Caja #")
                    .append(caja.getIdCaja())
                    .append(" | Cajero: ")
                    .append(caja.getUsuario().getNombreCompleto())
                    .append(" | Apertura: ")
                    .append(caja.getFechaApertura().format(formatoFecha))
                    .append(" | Total ventas: ")
                    .append(formatearMoneda(caja.getTotalVentas()));
        }

        return respuesta.toString();
    }

    private String responderUsuarioCajaPrincipal() {
        return reporteService.obtenerCajaPrincipal()
                .map(caja -> "El usuario de la caja principal es "
                        + caja.getUsuario().getNombreCompleto()
                        + " en la caja #"
                        + caja.getIdCaja()
                        + ", con "
                        + formatearMoneda(caja.getTotalVentas())
                        + " en ventas.")
                .orElse("No hay cajas abiertas para identificar una caja principal.");
    }

    private String responderUltimoCierreCaja() {
        return reporteService.obtenerUltimoCierreCaja()
                .map(caja -> "El ultimo cierre fue la caja #"
                        + caja.getIdCaja()
                        + " de "
                        + caja.getUsuario().getNombreCompleto()
                        + " el "
                        + caja.getFechaCierre().format(formatoFecha)
                        + ". Monto final: "
                        + formatearMoneda(caja.getMontoFinal())
                        + ".")
                .orElse("No hay cierres de caja registrados.");
    }

    private String responderDineroActualCaja() {
        BigDecimal dineroActual = reporteService.obtenerDineroActualCajasAbiertas();

        return "El dinero actual estimado en cajas abiertas es "
                + formatearMoneda(dineroActual)
                + ".";
    }

    private String responderCajeroMasVentasMes() {
        return reporteService.obtenerCajeroMasVentasMes()
                .map(cajero -> "El cajero con mas ventas este mes es "
                        + cajero.getCajero()
                        + " con "
                        + formatearMoneda(cajero.getVentas())
                        + " vendidos.")
                .orElse("Este mes no hay ventas registradas por cajeros.");
    }

    private String responderCajeroMasVentasHoy() {
        return reporteService.obtenerCajeroMasVentasHoy()
                .map(cajero -> "El cajero con mas ventas hoy es "
                        + cajero.getCajero()
                        + " con "
                        + formatearMoneda(cajero.getVentas())
                        + " vendidos.")
                .orElse("Hoy no hay ventas registradas por cajeros.");
    }

    private String responderVentasPorCajero() {
        List<ReporteCajeroDTO> cajeros = reporteService.obtenerVentasPorCajero();

        if (cajeros.isEmpty()) {
            return "No hay ventas registradas por cajero.";
        }

        return formatearReporteCajeros(
                "Ventas por cajero:",
                cajeros
        );
    }

    private String responderRankingCajeros() {
        List<ReporteCajeroDTO> cajeros = reporteService.obtenerRankingCajeros();

        if (cajeros.isEmpty()) {
            return "No hay ventas registradas por cajero.";
        }

        return formatearReporteCajeros(
                "Ranking de cajeros:",
                cajeros
        );
    }

    private String responderIngresosMetodoPago(String metodoPago, String nombreMetodo) {
        BigDecimal ingresos = reporteService.obtenerIngresosPorMetodoPago(metodoPago);

        return "Los ingresos por " + nombreMetodo + " son "
                + formatearMoneda(ingresos)
                + ".";
    }

    private String responderMetodoPagoMasUsado() {
        return reporteService.obtenerMetodoPagoMasUsado()
                .map(this::formatearMetodoPagoMasUsado)
                .orElse("No hay ventas registradas para identificar el metodo de pago mas usado.");
    }

    private String formatearMetodoPagoMasUsado(ReporteMetodoPagoDTO metodoPago) {
        long cantidadVentas = metodoPago.getTotal().longValue();

        if (cantidadVentas == 1) {
            return "El metodo de pago mas usado es "
                    + formatearMetodoPago(metodoPago.getMetodoPago())
                    + " con 1 venta.";
        }

        return "El metodo de pago mas usado es "
                + formatearMetodoPago(metodoPago.getMetodoPago())
                + " con "
                + cantidadVentas
                + " ventas.";
    }

    private String responderNombreEmpresa() {
        return reporteService.obtenerEmpresaActiva()
                .map(empresa -> "El nombre de la empresa es " + empresa.getNombreComercial() + ".")
                .orElse("No hay una empresa activa registrada.");
    }

    private String responderNitEmpresa() {
        return reporteService.obtenerEmpresaActiva()
                .map(empresa -> "El NIT de la empresa es " + empresa.getNit() + ".")
                .orElse("No hay una empresa activa registrada.");
    }

    private String responderTelefonoEmpresa() {
        return reporteService.obtenerEmpresaActiva()
                .map(this::formatearTelefonoEmpresa)
                .orElse("No hay una empresa activa registrada.");
    }

    private String formatearTelefonoEmpresa(Empresa empresa) {
        return "El telefono de la empresa es " + empresa.getTelefono() + ".";
    }

    private String formatearReporteCajeros(
            String titulo,
            List<ReporteCajeroDTO> cajeros
    ) {
        StringBuilder respuesta = new StringBuilder(titulo);

        int limite = Math.min(cajeros.size(), 5);
        for (int i = 0; i < limite; i++) {
            ReporteCajeroDTO cajero = cajeros.get(i);

            respuesta.append("\n")
                    .append(i + 1)
                    .append(". ")
                    .append(cajero.getCajero())
                    .append(" | Ventas: ")
                    .append(formatearMoneda(cajero.getVentas()));
        }

        if (cajeros.size() > limite) {
            respuesta.append("\n")
                    .append("Y ")
                    .append(cajeros.size() - limite)
                    .append(" cajeros mas.");
        }

        return respuesta.toString();
    }

    private String formatearMoneda(BigDecimal valor) {
        return formatoMoneda.format(valor);
    }

    private String formatearMetodoPago(String metodoPago) {
        if (metodoPago == null || metodoPago.isBlank()) {
            return "desconocido";
        }

        String texto = metodoPago.replace("_", " ").toLowerCase(Locale.ROOT);
        return texto.substring(0, 1).toUpperCase(Locale.ROOT) + texto.substring(1);
    }

    private String formatearFecha(Venta venta) {
        return venta.getFecha().format(formatoFecha);
    }
}
