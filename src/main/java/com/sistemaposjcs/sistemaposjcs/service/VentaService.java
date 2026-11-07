package com.sistemaposjcs.sistemaposjcs.service;

import com.sistemaposjcs.sistemaposjcs.model.Venta;
import com.sistemaposjcs.sistemaposjcs.model.Enum.MetodoPago;
import com.sistemaposjcs.sistemaposjcs.model.Cliente;
import com.sistemaposjcs.sistemaposjcs.model.ItemFactura;
import com.sistemaposjcs.sistemaposjcs.model.Producto;
import com.sistemaposjcs.sistemaposjcs.dto.VentaCajaDTO;
import com.sistemaposjcs.sistemaposjcs.model.Caja;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario;
import com.sistemaposjcs.sistemaposjcs.model.Enum.EstadoCaja;
import com.sistemaposjcs.sistemaposjcs.repository.VentaRepository;
import com.sistemaposjcs.sistemaposjcs.repository.ClienteRepository;
import com.sistemaposjcs.sistemaposjcs.repository.ProductoRepository;
import com.sistemaposjcs.sistemaposjcs.repository.CajaRepository;
import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.service.UsuarioAuthService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.math.RoundingMode;

@Service
public class VentaService {
    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final CajaRepository cajaRepository;
    private final InventarioService inventarioService;
    private final UsuarioAuthService usuarioAuthService;
    private final AuditoriaService auditoriaService;


    public VentaService(
            VentaRepository ventaRepository,
            ClienteRepository clienteRepository,
            ProductoRepository productoRepository,
            CajaRepository cajaRepository,
            InventarioService inventarioService,
            UsuarioAuthService usuarioAuthService,
            AuditoriaService auditoriaService
    ) {
        this.ventaRepository = ventaRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
        this.cajaRepository = cajaRepository;
        this.inventarioService = inventarioService;
        this.usuarioAuthService = usuarioAuthService;
        this.auditoriaService = auditoriaService;
    }
    
    // ✅ Obtener venta por ID
    public Venta getVentaById(Long id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
    }

    public Page<Venta> getActiveVentas(Pageable pageable){
        return ventaRepository.findByEstadoTrue(pageable);
    }

    public Page<Venta> getInactiveVentas(Pageable pageable){
        return ventaRepository.findByEstadoFalse(pageable);
    }

// Crear Venta
public Venta createVenta(Venta venta) {

    if (venta.getEstado() == null) {
    venta.setEstado(true);
    }

    // Validar cliente real
    Cliente clienteReal = clienteRepository.findById(
            venta.getCliente().getIdCliente()
    ).orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

    venta.setCliente(clienteReal);

    // 🔥 Obtener usuario real
    Usuario usuario = usuarioAuthService.getUsuarioLogueado();

    venta.setUsuario(usuario);

    // 🔥 Buscar caja abierta del usuario
    Caja caja = cajaRepository
            .findByEstadoCajaAndUsuarioIdUsuario(EstadoCaja.ABIERTA, usuario.getIdUsuario())
            .orElseThrow(() -> new RuntimeException("El usuario no tiene caja abierta"));

    venta.setCaja(caja);

    if (venta.getItems() == null || venta.getItems().isEmpty()) {
        throw new RuntimeException("La venta debe contener al menos un item");
    }


    BigDecimal totalVenta = BigDecimal.ZERO;
    BigDecimal totalIVA = BigDecimal.ZERO;
    BigDecimal totalSinIVA = BigDecimal.ZERO;

    for (ItemFactura item : venta.getItems()) {

        // Buscar producto real
        Producto producto = productoRepository.findById(
                item.getProducto().getIdProducto()
        ).orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Precio final (incluye IVA)
        BigDecimal precioFinal = producto.getPrecioventa();
        item.setPrecioUnitario(precioFinal);

        // Precio sin IVA
        BigDecimal precioSinIVA = producto.getPrecioSinIva();

        // IVA unitario = (precioConIVA - precioSinIVA)
        BigDecimal ivaUnitario = precioFinal.subtract(precioSinIVA);

        // Subtotal final (con IVA)
        BigDecimal subtotal = precioFinal.multiply(BigDecimal.valueOf(item.getCantidad()));
        item.setSubtotal(subtotal);

        // IVA total de este Item
        BigDecimal valorIVA = ivaUnitario.multiply(BigDecimal.valueOf(item.getCantidad()));
        item.setValorIVA(valorIVA);

        // Guardar el porcentaje de IVA
        item.setIvaPorcentaje(producto.getIva().getValue());

        // Acumular
        totalIVA = totalIVA.add(valorIVA);
        totalVenta = totalVenta.add(subtotal);
        totalSinIVA = totalSinIVA.add(
        precioSinIVA.multiply(BigDecimal.valueOf(item.getCantidad()))
);

        // Enlazar entidades
        item.setVenta(venta);
        item.setProducto(producto);
    }
    venta.setTotalIVA(totalIVA);
    venta.setTotal(totalVenta);
    venta.setTotalSinIVA(totalSinIVA);

    if (venta.getMetodoPago() == MetodoPago.EFECTIVO) {
        if (venta.getMontoRecibido() == null) {
            throw new RuntimeException("Debe enviar el monto recibido para pagos en efectivo.");
        }
        if (venta.getMontoRecibido().compareTo(totalVenta) < 0) {
            throw new RuntimeException("El monto recibido es insuficiente.");
        }
        venta.setCambio(venta.getMontoRecibido().subtract(totalVenta));
    }

    // 🔥 Actualizar totales de la caja
    caja.setTotalVentas(
            caja.getTotalVentas().add(totalVenta)
    );

    if (venta.getMetodoPago() == MetodoPago.EFECTIVO) {

        caja.setTotalEfectivo(
                caja.getTotalEfectivo().add(totalVenta)
        );

    } else if (venta.getMetodoPago() == MetodoPago.TRANSFERENCIA) {

        caja.setTotalTransferencia(
                caja.getTotalTransferencia().add(totalVenta)
        );
    }
    Venta ventaGuardada = ventaRepository.save(venta);

    // 🔥 Descontar inventario
    for (ItemFactura item : ventaGuardada.getItems()) {

    inventarioService.registrarVenta(
        item.getProducto().getIdProducto(),
        item.getCantidad(),
        "Venta ID: " + ventaGuardada.getIdVenta()
    );
    }

    try {

        int cantidadProductos = ventaGuardada.getItems().stream()
                .mapToInt(item -> item.getCantidad())
                .sum();

        String descripcion =
                "Venta #" + ventaGuardada.getIdVenta() + " registrada.\n" +
                "Cliente: " + ventaGuardada.getCliente().getNombreCompleto() + ".\n" +
                "Productos: " + cantidadProductos + ".\n" +
                "Total: $" + ventaGuardada.getTotal().setScale(0, java.math.RoundingMode.HALF_UP) + ".\n" +
                "Método de pago: " + ventaGuardada.getMetodoPago() + ".";

        auditoriaService.registrar(
                usuario,
                "VENTA",
                "CREAR",
                descripcion
        );

    } catch (Exception e) {
        System.out.println("Error auditoría venta: " + e.getMessage());
    }

    return ventaGuardada;
}

@Transactional
public void desactivarVenta(Long id) {

    Venta v = ventaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

    if (!v.getEstado()) {
        throw new RuntimeException("La venta ya está desactivada");
    }

    Caja caja = v.getCaja();

    BigDecimal total = v.getTotal();

    caja.setTotalVentas(
            caja.getTotalVentas().subtract(total)
    );

    if (v.getMetodoPago() == MetodoPago.EFECTIVO) {

        caja.setTotalEfectivo(
                caja.getTotalEfectivo().subtract(total)
        );

    } else if (v.getMetodoPago() == MetodoPago.TRANSFERENCIA) {

        caja.setTotalTransferencia(
                caja.getTotalTransferencia().subtract(total)
        );
    }

    v.setEstado(false);

    ventaRepository.save(v);

    try {

        Usuario usuario = usuarioAuthService.getUsuarioLogueado();

        String descripcion =
                "Venta #" + v.getIdVenta() + " desactivada.\n" +
                "Cliente: " + v.getCliente().getNombreCompleto() + ".\n" +
                "Total: $" + v.getTotal().setScale(0, RoundingMode.HALF_UP) + ".\n" +
                "Método de pago: " + v.getMetodoPago() + ".";

        auditoriaService.registrar(
                usuario,
                "VENTA",
                "DESACTIVAR",
                descripcion
        );

    } catch (Exception e) {
        System.out.println("Error auditoría venta: " + e.getMessage());
    }
}

@Transactional
public Venta activarVenta(Long id) {

    Venta v = ventaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

    if (v.getEstado()) {
        throw new RuntimeException("La venta ya está activa");
    }

    Caja caja = v.getCaja();
    BigDecimal total = v.getTotal();

    caja.setTotalVentas(
            caja.getTotalVentas().add(total)
    );

    if (v.getMetodoPago() == MetodoPago.EFECTIVO) {

        caja.setTotalEfectivo(
                caja.getTotalEfectivo().add(total)
        );

    } else if (v.getMetodoPago() == MetodoPago.TRANSFERENCIA) {

        caja.setTotalTransferencia(
                caja.getTotalTransferencia().add(total)
        );
    }

    v.setEstado(true);

    Venta ventaActivada = ventaRepository.save(v);

    try {

        Usuario usuario = usuarioAuthService.getUsuarioLogueado();

        String descripcion =
                "Venta #" + ventaActivada.getIdVenta() + " reactivada.\n" +
                "Cliente: " + ventaActivada.getCliente().getNombreCompleto() + ".\n" +
                "Total: $" + ventaActivada.getTotal().setScale(0, RoundingMode.HALF_UP) + ".\n" +
                "Método de pago: " + ventaActivada.getMetodoPago() + ".";

        auditoriaService.registrar(
                usuario,
                "VENTA",
                "ACTIVAR",
                descripcion
        );

    } catch (Exception e) {
        System.out.println("Error auditoría venta: " + e.getMessage());
    }

    return ventaActivada;
}

@Transactional
public Venta cambiarMetodoPago(Long id, MetodoPago nuevoMetodo) {

    Venta venta = getVentaById(id);

    MetodoPago metodoAnterior = venta.getMetodoPago();
    BigDecimal total = venta.getTotal();
    Caja caja = venta.getCaja();

    if (metodoAnterior == nuevoMetodo) {
        return venta;
    }

    // Restar del método anterior
    if (metodoAnterior == MetodoPago.EFECTIVO) {
        caja.setTotalEfectivo(caja.getTotalEfectivo().subtract(total));
    } else if (metodoAnterior == MetodoPago.TRANSFERENCIA) {
        caja.setTotalTransferencia(caja.getTotalTransferencia().subtract(total));
    }

    // Sumar al nuevo método
    if (nuevoMetodo == MetodoPago.EFECTIVO) {
        caja.setTotalEfectivo(caja.getTotalEfectivo().add(total));
    } else if (nuevoMetodo == MetodoPago.TRANSFERENCIA) {
        caja.setTotalTransferencia(caja.getTotalTransferencia().add(total));
    }

    venta.setMetodoPago(nuevoMetodo);

    Venta ventaActualizada = ventaRepository.save(venta);

    try {

        Usuario usuario = usuarioAuthService.getUsuarioLogueado();

        String descripcion =
                "Venta #" + ventaActualizada.getIdVenta() + ".\n" +
                "Cliente: " + ventaActualizada.getCliente().getNombreCompleto() + ".\n" +
                "Método de pago cambiado de " +
                metodoAnterior + " a " + nuevoMetodo + ".";

        auditoriaService.registrar(
                usuario,
                "VENTA",
                "CAMBIAR MÉTODO DE PAGO",
                descripcion
        );

    } catch (Exception e) {
        System.out.println("Error auditoría venta: " + e.getMessage());
    }

    return ventaActualizada;
}

public Page<Venta> searchVentas(
    Pageable pageable,
    String search,
    String metodoPago,
    Boolean estado,
    Long idCaja,
    LocalDate fechaInicio,
    LocalDate fechaFin
) {
    Specification<Venta> spec = buildSpec(search, metodoPago, estado, idCaja, fechaInicio, fechaFin);
    return ventaRepository.findAll(spec, pageable);
}

public Specification<Venta> buildSpec(
    String search,
    String metodoPago,
    Boolean estado,
    Long idCaja,
    LocalDate fechaInicio,
    LocalDate fechaFin
) {
    return (root, query, cb) -> {

        List<Predicate> predicates = new ArrayList<>();

        // 🔍 búsqueda global
        if (search != null && !search.isEmpty()) {
            String searchLower = "%" + search.toLowerCase() + "%";

            // 1. Concatenación Nombre Cliente
            Join<Venta, Cliente> cliente = root.join("cliente"); 
            // NOTA: Aquí usa el nombre exacto de la variable en la clase Cliente
            Expression<String> nombreCompletoCliente = cb.concat(cb.lower(cliente.get("primerNombre")), " ");
            nombreCompletoCliente = cb.concat(nombreCompletoCliente, cb.lower(cb.coalesce(cliente.get("segundoNombre"), "")));
            nombreCompletoCliente = cb.concat(nombreCompletoCliente, " ");
            nombreCompletoCliente = cb.concat(nombreCompletoCliente, cb.lower(cliente.get("primerApellido")));
            nombreCompletoCliente = cb.concat(nombreCompletoCliente, " ");
            nombreCompletoCliente = cb.concat(nombreCompletoCliente, cb.lower(cb.coalesce(cliente.get("segundoApellido"), "")));

            // 2. Concatenación Nombre Cajero
            Join<Venta, Usuario> cajero = root.join("usuario"); 
            Expression<String> nombreCompletoCajero = cb.concat(cb.lower(cajero.get("primerNombre")), " ");
            nombreCompletoCajero = cb.concat(nombreCompletoCajero, cb.lower(cb.coalesce(cajero.get("segundoNombre"), "")));
            nombreCompletoCajero = cb.concat(nombreCompletoCajero, " ");
            nombreCompletoCajero = cb.concat(nombreCompletoCajero, cb.lower(cajero.get("primerApellido")));
            nombreCompletoCajero = cb.concat(nombreCompletoCajero, " ");
            nombreCompletoCajero = cb.concat(nombreCompletoCajero, cb.lower(cajero.get("segundoApellido")));

            // 3. Predicados
            predicates.add(cb.or(
                cb.like(nombreCompletoCliente, searchLower),
                cb.like(nombreCompletoCajero, searchLower),
                cb.like(cliente.get("documento"), "%" + search + "%")
            ));
        }

        // 💳 método pago
        if (metodoPago != null && !metodoPago.isEmpty()) {
            predicates.add(cb.equal(root.get("metodoPago"), metodoPago));
        }

        // 🟢 estado
        if (estado != null) {
            predicates.add(cb.equal(root.get("estado"), estado));
        }

        if (idCaja != null) {
                predicates.add(
                    cb.equal(root.get("caja").get("idCaja"), idCaja)
                );
        }

        // 📅 fechas
        if (fechaInicio != null && fechaFin != null) {
            predicates.add(cb.between(
                root.get("fecha"),
                fechaInicio.atStartOfDay(),
                fechaFin.atTime(23, 59)
            ));
        }
        // Si solo viene la de apertura, buscamos de ahí en adelante
        else if (fechaInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaInicio"), fechaInicio.atStartOfDay()));
        }
        // Si solo viene la de cierre, buscamos hasta ese día
        else if (fechaFin != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaFin"), fechaFin.atTime(23, 59, 59)));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    };
}

public List<VentaCajaDTO> obtenerVentasPorCaja(Long idCaja) {

    List<Venta> ventas = ventaRepository.findByCajaIdCaja(idCaja);

    return ventas.stream()
            .map(v -> new VentaCajaDTO(
                    v.getIdVenta(),
                    v.getFecha(),
                    v.getMetodoPago().name(),
                    v.getTotal()
            ))
            .toList();
}
 
}
