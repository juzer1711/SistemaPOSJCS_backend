package com.sistemaposjcs.sistemaposjcs.service;
import org.springframework.stereotype.Service;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario;

// Clases nativas de Java (JDK)
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Sort;

import com.sistemaposjcs.sistemaposjcs.repository.VentaRepository;
import com.sistemaposjcs.sistemaposjcs.repository.CajaRepository;
import com.sistemaposjcs.sistemaposjcs.repository.ClienteRepository;
import com.sistemaposjcs.sistemaposjcs.repository.ProductoRepository;
import com.sistemaposjcs.sistemaposjcs.repository.AuditoriaRepository;
import com.sistemaposjcs.sistemaposjcs.model.Auditoria;
import com.sistemaposjcs.sistemaposjcs.model.Caja;
import com.sistemaposjcs.sistemaposjcs.model.Cliente;
import com.sistemaposjcs.sistemaposjcs.model.Producto;
import com.sistemaposjcs.sistemaposjcs.model.Venta;
import com.sistemaposjcs.sistemaposjcs.model.Enum.EstadoCaja;
import com.sistemaposjcs.sistemaposjcs.model.Enum.TipoDocumento;

// Clases de Apache POI (Para manejo de Excel)
import org.apache.poi.ss.usermodel.*;

@Service
public class ExportService {

    private final VentaRepository ventaRepository;
    private final VentaService ventaService;
    private final CajaRepository cajaRepository;
    private final CajaService cajaService;
    private final ClienteRepository  clienteRepository;
    private final ClienteService     clienteService;
    private final ProductoRepository productoRepository;
    private final ProductoService    productoService;
    private final AuditoriaRepository auditoriaRepository;
    private final AuditoriaService auditoriaService;

    public ExportService(VentaRepository ventaRepository, VentaService ventaService, 
        CajaRepository cajaRepository, CajaService cajaService, 
        ClienteRepository clienteRepository, ClienteService clienteService,
        ProductoRepository productoRepository, ProductoService productoService,
        AuditoriaRepository auditoriaRepository, AuditoriaService auditoriaService) {
        this.ventaRepository = ventaRepository; this.ventaService = ventaService;
        this.cajaRepository = cajaRepository; this.cajaService = cajaService;
        this.clienteRepository = clienteRepository; this.clienteService = clienteService;
        this.productoRepository= productoRepository;this.productoService= productoService;
        this.auditoriaRepository = auditoriaRepository; this.auditoriaService = auditoriaService;
    }

    // ── VENTAS → EXCEL ───────────────────────────────────────────────
    public byte[] exportVentasExcel(
            String search,
            String metodoPago,
            Boolean estado,
            Long idCaja,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) throws IOException {

        // Reutilizás tu buildSpec existente en VentaService
        List<Venta> ventas = ventaRepository.findAll(
            ventaService.buildSpec(search, metodoPago, estado, idCaja, fechaInicio, fechaFin)
        );

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Ventas");

        // ── Estilo encabezado ──
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // ── Encabezados ──
        String[] cols = {
            "ID", "Fecha", "Cliente", "Documento Cliente",
            "Cajero", "Caja", "Método Pago",
            "Total Sin IVA", "IVA", "Total",
            "Monto Recibido", "Cambio", "Estado", "Observaciones"
        };

        Row header = sheet.createRow(0);
        for (int i = 0; i < cols.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(cols[i]);
            cell.setCellStyle(headerStyle);
        }

        // ── Datos ──
        int rowIdx = 1;
        for (Venta v : ventas) {

            String nombreCliente = v.getCliente().getRazonSocial() != null
                ? v.getCliente().getRazonSocial()
                : (v.getCliente().getPrimerNombre() + " " + v.getCliente().getPrimerApellido()).trim();

            String nombreCajero = v.getUsuario().getPrimerNombre()
                + " " + v.getUsuario().getPrimerApellido();

            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(v.getIdVenta());
            row.createCell(1).setCellValue(v.getFecha().toString());
            row.createCell(2).setCellValue(nombreCliente);
            row.createCell(3).setCellValue(v.getCliente().getDocumento());
            row.createCell(4).setCellValue(nombreCajero);
            row.createCell(5).setCellValue(v.getCaja().getIdCaja());
            row.createCell(6).setCellValue(v.getMetodoPago().name());
            row.createCell(7).setCellValue(v.getTotalSinIVA().doubleValue());
            row.createCell(8).setCellValue(v.getTotalIVA().doubleValue());
            row.createCell(9).setCellValue(v.getTotal().doubleValue());
            row.createCell(10).setCellValue(
                v.getMontoRecibido() != null ? v.getMontoRecibido().doubleValue() : 0
            );
            row.createCell(11).setCellValue(
                v.getCambio() != null ? v.getCambio().doubleValue() : 0
            );
            row.createCell(12).setCellValue(v.getEstado() ? "Activa" : "Inactiva");
            row.createCell(13).setCellValue(
                v.getObservaciones() != null ? v.getObservaciones() : ""
            );
        }

        // Autoajustar columnas
        for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out.toByteArray();
    }

    // ── VENTAS → CSV ─────────────────────────────────────────────────
    public byte[] exportVentasCSV(
            String search,
            String metodoPago,
            Boolean estado,
            Long idCaja,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {
        List<Venta> ventas = ventaRepository.findAll(
            ventaService.buildSpec(search, metodoPago, estado, idCaja, fechaInicio, fechaFin)
        );

        StringBuilder sb = new StringBuilder();
        sb.append("ID,Fecha,Cliente,Documento,Cajero,Caja,MetodoPago,")
          .append("TotalSinIVA,IVA,Total,MontoRecibido,Cambio,Estado\n");

        for (Venta v : ventas) {

            String nombreCliente = v.getCliente().getRazonSocial() != null
                ? v.getCliente().getRazonSocial()
                : (v.getCliente().getPrimerNombre() + " " + v.getCliente().getPrimerApellido()).trim();

            sb.append(v.getIdVenta()).append(",")
              .append(v.getFecha()).append(",")
              .append(nombreCliente).append(",")
              .append(v.getCliente().getDocumento()).append(",")
              .append(v.getUsuario().getPrimerNombre())
                  .append(" ").append(v.getUsuario().getPrimerApellido()).append(",")
              .append(v.getCaja().getIdCaja()).append(",")
              .append(v.getMetodoPago().name()).append(",")
              .append(v.getTotalSinIVA()).append(",")
              .append(v.getTotalIVA()).append(",")
              .append(v.getTotal()).append(",")
              .append(v.getMontoRecibido() != null ? v.getMontoRecibido() : 0).append(",")
              .append(v.getCambio() != null ? v.getCambio() : 0).append(",")
              .append(v.getEstado() ? "Activa" : "Inactiva").append("\n");
        }

        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
    // ── CAJAS → EXCEL ────────────────────────────────────────────────
public byte[] exportCajasExcel(
        String search,
        Long idCaja,
        LocalDate fechaApertura,
        LocalDate fechaCierre,
        EstadoCaja estado
) throws IOException {

    List<Caja> cajas = cajaRepository.findAll(
        cajaService.buildSpec(search, idCaja, fechaApertura, fechaCierre, estado)
    );

    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Cajas");

    CellStyle headerStyle = workbook.createCellStyle();
    Font headerFont = workbook.createFont();
    headerFont.setBold(true);
    headerStyle.setFont(headerFont);
    headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

    String[] cols = {
        "ID Caja", "Cajero", "Estado",
        "Fecha Apertura", "Fecha Cierre",
        "Monto Inicial", "Total Ventas",
        "Total Efectivo", "Total Transferencia",
        "Efectivo Real", "Transferencia Real",
        "Diferencia Efectivo", "Diferencia Transferencia",
        "Monto Final"
    };

    Row header = sheet.createRow(0);
    for (int i = 0; i < cols.length; i++) {
        Cell cell = header.createCell(i);
        cell.setCellValue(cols[i]);
        cell.setCellStyle(headerStyle);
    }

    int rowIdx = 1;
    for (Caja c : cajas) {

        String nombreCajero = c.getUsuario().getPrimerNombre()
            + " " + c.getUsuario().getPrimerApellido();

        Row row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue(c.getIdCaja());
        row.createCell(1).setCellValue(nombreCajero);
        row.createCell(2).setCellValue(c.getEstadoCaja().name());
        row.createCell(3).setCellValue(
            c.getFechaApertura() != null ? c.getFechaApertura().toString() : ""
        );
        row.createCell(4).setCellValue(
            c.getFechaCierre() != null ? c.getFechaCierre().toString() : ""
        );
        row.createCell(5).setCellValue(c.getMontoInicial().doubleValue());
        row.createCell(6).setCellValue(c.getTotalVentas().doubleValue());
        row.createCell(7).setCellValue(c.getTotalEfectivo().doubleValue());
        row.createCell(8).setCellValue(c.getTotalTransferencia().doubleValue());
        row.createCell(9).setCellValue(
            c.getEfectivoReal() != null ? c.getEfectivoReal().doubleValue() : 0
        );
        row.createCell(10).setCellValue(
            c.getTransferenciaReal() != null ? c.getTransferenciaReal().doubleValue() : 0
        );
        row.createCell(11).setCellValue(
            c.getDiferenciaEfectivo() != null ? c.getDiferenciaEfectivo().doubleValue() : 0
        );
        row.createCell(12).setCellValue(
            c.getDiferenciaTransferencia() != null ? c.getDiferenciaTransferencia().doubleValue() : 0
        );
        row.createCell(13).setCellValue(c.getMontoFinal().doubleValue());
    }

    for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    workbook.write(out);
    workbook.close();
    return out.toByteArray();
}

// ── CAJAS → CSV ──────────────────────────────────────────────────
public byte[] exportCajasCSV(
        String search,
        Long idCaja,
        LocalDate fechaApertura,
        LocalDate fechaCierre,
        EstadoCaja estado
) {
    List<Caja> cajas = cajaRepository.findAll(
        cajaService.buildSpec(search, idCaja, fechaApertura, fechaCierre, estado)
    );

    StringBuilder sb = new StringBuilder();
    sb.append("ID,Cajero,Estado,FechaApertura,FechaCierre,")
      .append("MontoInicial,TotalVentas,TotalEfectivo,TotalTransferencia,")
      .append("EfectivoReal,TransferenciaReal,DiferenciaEfectivo,DiferenciaTransferencia,MontoFinal\n");

    for (Caja c : cajas) {

        String nombreCajero = c.getUsuario().getPrimerNombre()
            + " " + c.getUsuario().getPrimerApellido();

        sb.append(c.getIdCaja()).append(",")
          .append(nombreCajero).append(",")
          .append(c.getEstadoCaja().name()).append(",")
          .append(c.getFechaApertura() != null ? c.getFechaApertura() : "").append(",")
          .append(c.getFechaCierre()   != null ? c.getFechaCierre()   : "").append(",")
          .append(c.getMontoInicial()).append(",")
          .append(c.getTotalVentas()).append(",")
          .append(c.getTotalEfectivo()).append(",")
          .append(c.getTotalTransferencia()).append(",")
          .append(c.getEfectivoReal()            != null ? c.getEfectivoReal()            : 0).append(",")
          .append(c.getTransferenciaReal()        != null ? c.getTransferenciaReal()        : 0).append(",")
          .append(c.getDiferenciaEfectivo()       != null ? c.getDiferenciaEfectivo()       : 0).append(",")
          .append(c.getDiferenciaTransferencia()  != null ? c.getDiferenciaTransferencia()  : 0).append(",")
          .append(c.getMontoFinal()).append("\n");
    }

    return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
}
// ════════════════════════════════════════════════════════
// CLIENTES
// ════════════════════════════════════════════════════════
public byte[] exportClientesExcel(
        String search, String tipoCliente,
        String tipoDocumento, Boolean estado
) throws IOException {

    List<Cliente> clientes = clienteRepository.findAll(
        clienteService.buildSpec(search, tipoCliente, tipoDocumento, estado)
    );

    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Clientes");

    CellStyle headerStyle = workbook.createCellStyle();
    Font headerFont = workbook.createFont();
    headerFont.setBold(true);
    headerStyle.setFont(headerFont);
    headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

    String[] cols = {
        "ID", "Tipo Cliente", "Nombre / Razón Social", "Tipo Documento",
        "Documento", "Email", "Teléfono", "Dirección", "Estado"
    };

    Row header = sheet.createRow(0);
    for (int i = 0; i < cols.length; i++) {
        Cell cell = header.createCell(i);
        cell.setCellValue(cols[i]);
        cell.setCellStyle(headerStyle);
    }

    int rowIdx = 1;
    for (Cliente c : clientes) {
        Row row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue(c.getIdCliente());
        row.createCell(1).setCellValue(c.getTipoCliente().name());
        row.createCell(2).setCellValue(c.getNombreCompleto());
        row.createCell(3).setCellValue(c.getTipoDocumento().name());
        row.createCell(4).setCellValue(c.getDocumento());
        row.createCell(5).setCellValue(c.getEmail()     != null ? c.getEmail()     : "");
        row.createCell(6).setCellValue(c.getTelefono()  != null ? c.getTelefono()  : "");
        row.createCell(7).setCellValue(c.getDireccion() != null ? c.getDireccion() : "");
        row.createCell(8).setCellValue(c.getEstado() ? "Activo" : "Inactivo");
    }

    for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    workbook.write(out);
    workbook.close();
    return out.toByteArray();
}

public byte[] exportClientesCSV(
        String search, String tipoCliente,
        String tipoDocumento, Boolean estado
) {
    List<Cliente> clientes = clienteRepository.findAll(
        clienteService.buildSpec(search, tipoCliente, tipoDocumento, estado)
    );

    StringBuilder sb = new StringBuilder();
    sb.append("ID,TipoCliente,Nombre,TipoDocumento,Documento,Email,Telefono,Direccion,Estado\n");

    for (Cliente c : clientes) {
        sb.append(c.getIdCliente()).append(",")
          .append(c.getTipoCliente().name()).append(",")
          .append(c.getNombreCompleto()).append(",")
          .append(c.getTipoDocumento().name()).append(",")
          .append(c.getDocumento()).append(",")
          .append(c.getEmail()     != null ? c.getEmail()     : "").append(",")
          .append(c.getTelefono()  != null ? c.getTelefono()  : "").append(",")
          .append(c.getDireccion() != null ? c.getDireccion() : "").append(",")
          .append(c.getEstado() ? "Activo" : "Inactivo").append("\n");
    }

    return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
}

// ════════════════════════════════════════════════════════
// PRODUCTOS
// ════════════════════════════════════════════════════════
public byte[] exportProductosExcel(
        String search, Long categoria, Boolean estado
) throws IOException {

    List<Producto> productos = productoRepository.findAll(
        productoService.buildSpec(search, categoria, estado)
    );

    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Productos");

    CellStyle headerStyle = workbook.createCellStyle();
    Font headerFont = workbook.createFont();
    headerFont.setBold(true);
    headerStyle.setFont(headerFont);
    headerStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

    String[] cols = {
        "ID", "Nombre", "Categoría", "Código Barras", "Descripción",
        "Costo", "Precio Venta", "Precio Sin IVA", "IVA",
        "Stock Actual", "Stock Mínimo", "Estado"
    };

    Row header = sheet.createRow(0);
    for (int i = 0; i < cols.length; i++) {
        Cell cell = header.createCell(i);
        cell.setCellValue(cols[i]);
        cell.setCellStyle(headerStyle);
    }

    int rowIdx = 1;
    for (Producto p : productos) {
        Row row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue(p.getIdProducto());
        row.createCell(1).setCellValue(p.getNombre());
        row.createCell(2).setCellValue(p.getCategoria().getNombre());
        row.createCell(3).setCellValue(p.getCodigoBarras());
        row.createCell(4).setCellValue(p.getDescripcion() != null ? p.getDescripcion() : "");
        row.createCell(5).setCellValue(p.getCosto().doubleValue());
        row.createCell(6).setCellValue(p.getPrecioventa().doubleValue());
        row.createCell(7).setCellValue(p.getPrecioSinIva().doubleValue());
        row.createCell(8).setCellValue(p.getIva().name());
        row.createCell(9).setCellValue(p.getStockActual());
        row.createCell(10).setCellValue(p.getStockMinimo());
        row.createCell(11).setCellValue(p.getEstado() ? "Activo" : "Inactivo");
    }

    for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    workbook.write(out);
    workbook.close();
    return out.toByteArray();
}

public byte[] exportProductosCSV(
        String search, Long categoria, Boolean estado
) {
    List<Producto> productos = productoRepository.findAll(
        productoService.buildSpec(search, categoria, estado)
    );

    StringBuilder sb = new StringBuilder();
    sb.append("ID,Nombre,Categoria,CodigoBarras,Descripcion,")
      .append("Costo,PrecioVenta,PrecioSinIVA,IVA,StockActual,StockMinimo,Estado\n");

    for (Producto p : productos) {
        sb.append(p.getIdProducto()).append(",")
          .append(p.getNombre()).append(",")
          .append(p.getCategoria().getNombre()).append(",")
          .append(p.getCodigoBarras()).append(",")
          .append(p.getDescripcion() != null ? p.getDescripcion() : "").append(",")
          .append(p.getCosto()).append(",")
          .append(p.getPrecioventa()).append(",")
          .append(p.getPrecioSinIva()).append(",")
          .append(p.getIva().name()).append(",")
          .append(p.getStockActual()).append(",")
          .append(p.getStockMinimo()).append(",")
          .append(p.getEstado() ? "Activo" : "Inactivo").append("\n");
    }

    return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
}

// ════════════════════════════════════════════════════════
// USUARIOS
// ════════════════════════════════════════════════════════
// ── USUARIOS → EXCEL ─────────────────────────────────────────────
public byte[] exportAuditoriaExcel(
        String search,
        String usuario,
        String modulo,
        String accion,
        String fechaInicio,
        String fechaFin
) throws IOException {

    List<Auditoria> auditorias = auditoriaRepository.findAll(
        auditoriaService.buildSpec(search, usuario, modulo, accion, fechaInicio, fechaFin),
        Sort.by(Sort.Direction.DESC, "fecha")
    );

    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Auditoria");

    CellStyle headerStyle = workbook.createCellStyle();
    Font headerFont = workbook.createFont();
    headerFont.setBold(true);
    headerStyle.setFont(headerFont);
    headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

    String[] cols = {
        "ID", "Fecha", "Usuario", "Nombre Completo", "Modulo", "Accion", "Descripcion"
    };

    Row header = sheet.createRow(0);
    for (int i = 0; i < cols.length; i++) {
        Cell cell = header.createCell(i);
        cell.setCellValue(cols[i]);
        cell.setCellStyle(headerStyle);
    }

    int rowIdx = 1;
    for (Auditoria a : auditorias) {
        Usuario u = a.getUsuario();
        Row row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue(a.getId());
        row.createCell(1).setCellValue(a.getFecha() != null ? a.getFecha().toString() : "");
        row.createCell(2).setCellValue(u != null ? u.getUsername() : "");
        row.createCell(3).setCellValue(u != null ? u.getNombreCompleto() : "");
        row.createCell(4).setCellValue(a.getModulo() != null ? a.getModulo() : "");
        row.createCell(5).setCellValue(a.getAccion() != null ? a.getAccion() : "");
        row.createCell(6).setCellValue(a.getDescripcion() != null ? a.getDescripcion() : "");
    }

    for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    workbook.write(out);
    workbook.close();
    return out.toByteArray();
}

public byte[] exportAuditoriaCSV(
        String search,
        String usuario,
        String modulo,
        String accion,
        String fechaInicio,
        String fechaFin
) {
    List<Auditoria> auditorias = auditoriaRepository.findAll(
        auditoriaService.buildSpec(search, usuario, modulo, accion, fechaInicio, fechaFin),
        Sort.by(Sort.Direction.DESC, "fecha")
    );

    StringBuilder sb = new StringBuilder();
    sb.append("ID,Fecha,Usuario,NombreCompleto,Modulo,Accion,Descripcion\n");

    for (Auditoria a : auditorias) {
        Usuario u = a.getUsuario();
        sb.append(csv(a.getId())).append(",")
          .append(csv(a.getFecha() != null ? a.getFecha().toString() : "")).append(",")
          .append(csv(u != null ? u.getUsername() : "")).append(",")
          .append(csv(u != null ? u.getNombreCompleto() : "")).append(",")
          .append(csv(a.getModulo())).append(",")
          .append(csv(a.getAccion())).append(",")
          .append(csv(a.getDescripcion())).append("\n");
    }

    return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
}

private String csv(Object value) {
    String text = value == null ? "" : value.toString();
    return "\"" + text.replace("\"", "\"\"") + "\"";
}
}
