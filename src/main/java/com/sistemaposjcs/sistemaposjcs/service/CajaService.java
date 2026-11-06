package com.sistemaposjcs.sistemaposjcs.service;

import com.sistemaposjcs.sistemaposjcs.model.Caja;
import com.sistemaposjcs.sistemaposjcs.model.Venta;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.repository.UsuarioRepository;
import com.sistemaposjcs.sistemaposjcs.model.Enum.EstadoCaja;
import com.sistemaposjcs.sistemaposjcs.model.Enum.MetodoPago;
import com.sistemaposjcs.sistemaposjcs.repository.CajaRepository;
import com.sistemaposjcs.sistemaposjcs.repository.VentaRepository;
import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.service.UsuarioAuthService;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.math.RoundingMode;

import static com.sistemaposjcs.sistemaposjcs.specification.SpecificationUtils.nombreCompleto;

@Service
public class CajaService {

    private final CajaRepository cajaRepository;
    private final UsuarioRepository userRepository;
    private final VentaRepository ventaRepository;
    private final AuditoriaService auditoriaService;
    private final UsuarioAuthService usuarioAuthService;

    public CajaService(
            CajaRepository cajaRepository,
            UsuarioRepository userRepository,
            VentaRepository ventaRepository,
            AuditoriaService auditoriaService,
            UsuarioAuthService usuarioAuthService
    ) {
        this.cajaRepository = cajaRepository;
        this.userRepository = userRepository;
        this.ventaRepository = ventaRepository;
        this.auditoriaService = auditoriaService;
        this.usuarioAuthService = usuarioAuthService;
    }

    // Obtener caja por ID
    public Caja getCajaById(Long id) {
        return cajaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Caja no encontrada"));
    }

    // Obtener caja por ID usuario
    public Caja obtenerCajaActivaPorUsuario(Long idUsuario) {

        return cajaRepository
            .findByEstadoCajaAndUsuarioIdUsuario(EstadoCaja.ABIERTA, idUsuario)
            .orElseThrow(() -> new RuntimeException("El usuario no tiene una caja abierta"));

    }

    // Listar cajas abiertas
    public Page<Caja> getCajasAbiertas(Pageable pageable) {
        return cajaRepository.findByEstadoCaja(pageable, EstadoCaja.ABIERTA);
    }
    // Listar cajas cerradas
    public Page<Caja> getCajasCerradas(Pageable pageable) {
        return cajaRepository.findByEstadoCaja(pageable, EstadoCaja.CERRADA);
    }

    // Abrir caja
    public Caja abrirCaja(Long idUsuario, BigDecimal montoInicial) {

        Usuario usuario = userRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar que el cajero no tenga otra caja abierta
        if (cajaRepository.existsByUsuarioIdUsuarioAndEstadoCaja(idUsuario, EstadoCaja.ABIERTA)) {
            throw new RuntimeException("El cajero ya tiene una caja abierta");
        }

        Caja caja = new Caja();
        caja.setUsuario(usuario);
        caja.setMontoInicial(montoInicial);
        caja.setEstadoCaja(EstadoCaja.ABIERTA);
        caja.setFechaApertura(LocalDateTime.now());

        Caja cajaGuardada = cajaRepository.save(caja);

        try {

            Usuario usuarioLogueado = usuarioAuthService.getUsuarioLogueado();

            String descripcion =
                    "Caja #" + cajaGuardada.getIdCaja() + " abierta.\n" +
                    "Cajero: " + usuario.getNombreCompleto() + ".\n" +
                    "Monto inicial: $" + montoInicial.setScale(0, RoundingMode.HALF_UP) + ".";

            auditoriaService.registrar(
                    usuarioLogueado,
                    "CAJA",
                    "ABRIR",
                    descripcion
            );

        } catch (Exception e) {
            System.out.println("Error auditoría caja: " + e.getMessage());
        }

        return cajaGuardada;
    }

    // Cerrar caja
    @Transactional
    public Caja cerrarCaja(Long idUsuario, BigDecimal efectivoReal, BigDecimal transferenciaReal) {

        Caja caja = cajaRepository
            .findByEstadoCajaAndUsuarioIdUsuario(EstadoCaja.ABIERTA, idUsuario)
            .orElseThrow(() -> new RuntimeException("No hay caja abierta"));

        if (caja.getEstadoCaja() == EstadoCaja.CERRADA) {
            throw new RuntimeException("La caja ya está cerrada");
        }

        // Calcular diferencias
        BigDecimal diferenciaEfectivo = efectivoReal.subtract(caja.getTotalEfectivo());
        BigDecimal diferenciaTransferencia = transferenciaReal.subtract(caja.getTotalTransferencia());

        // Guardar datos reales
        caja.setEfectivoReal(efectivoReal);
        caja.setTransferenciaReal(transferenciaReal);
        caja.setDiferenciaEfectivo(diferenciaEfectivo);
        caja.setDiferenciaTransferencia(diferenciaTransferencia);

        // Total final real
        BigDecimal montoFinal = efectivoReal.add(transferenciaReal);

        caja.setMontoFinal(montoFinal);
        caja.setFechaCierre(LocalDateTime.now());
        caja.setEstadoCaja(EstadoCaja.CERRADA);

        Caja cajaCerrada = cajaRepository.save(caja);

        try {

            Usuario usuario = usuarioAuthService.getUsuarioLogueado();

            String descripcion =
                    "Caja #" + cajaCerrada.getIdCaja() + " cerrada.\n" +
                    "Total ventas: $" + cajaCerrada.getTotalVentas().setScale(0, RoundingMode.HALF_UP) + ".\n" +
                    "Efectivo esperado: $" + cajaCerrada.getTotalEfectivo().setScale(0, RoundingMode.HALF_UP) + ".\n" +
                    "Transferencia esperada: $" + cajaCerrada.getTotalTransferencia().setScale(0, RoundingMode.HALF_UP) + ".\n" +
                    "Diferencia efectivo: $" + cajaCerrada.getDiferenciaEfectivo().setScale(0, RoundingMode.HALF_UP) + ".\n" +
                    "Diferencia transferencia: $" + cajaCerrada.getDiferenciaTransferencia().setScale(0, RoundingMode.HALF_UP) + ".";

            auditoriaService.registrar(
                    usuario,
                    "CAJA",
                    "CERRAR",
                    descripcion
            );

        } catch (Exception e) {
            System.out.println("Error auditoría caja: " + e.getMessage());
        }

        return cajaCerrada;
    }

    public Page<Caja> searchCajas(
        Pageable pageable,
        String search,
        Long idCaja,
        LocalDate fechaApertura,
        LocalDate fechaCierre,
        EstadoCaja estado
    ) {
        Specification<Caja> spec = buildSpec(search, idCaja, fechaApertura, fechaCierre, estado);
        return cajaRepository.findAll(spec, pageable);
    }

    public Specification<Caja> buildSpec(
        String search,
        Long idCaja,
        LocalDate fechaApertura,
        LocalDate fechaCierre,
        EstadoCaja estado
    ) {
        return (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();

                // búsqueda global (nombre o email)
                if (search != null && !search.isEmpty()) {
                    String like = "%" + search.toLowerCase().replace(" ", "") + "%";

                        Join<Caja, Usuario> usuario = root.join("usuario");
                        Expression<String> nombre = nombreCompleto(cb, usuario);

                        predicates.add(cb.like(nombre, like));
                    }
            
            if (estado != null) {
                    predicates.add(cb.equal(root.get("estadoCaja"), estado));
                }
            if (idCaja != null) {
                predicates.add(cb.equal(root.get("idCaja"), idCaja));
            }
            // fechas
            if (fechaApertura != null && fechaCierre != null) {
                predicates.add(cb.between(
                    root.get("fecha"),
                    fechaApertura.atStartOfDay(),
                    fechaCierre.atTime(23, 59)
                ));
            }
            // Si solo viene la de apertura, buscamos de ahí en adelante
            else if (fechaApertura != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaApertura"), fechaApertura.atStartOfDay()));
            }
            // Si solo viene la de cierre, buscamos hasta ese día
            else if (fechaCierre != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaApertura"), fechaCierre.atTime(23, 59, 59)));
            }

                return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional
public void cerrarCajaForzadoAdmin(Long idCaja) {

    Caja caja = cajaRepository.findById(idCaja)
            .orElseThrow(() -> new RuntimeException("Caja no encontrada"));

    if (!caja.getEstadoCaja().equals(EstadoCaja.ABIERTA)) {
        throw new RuntimeException("La caja ya está cerrada");
    }

    // Traer todas las ventas de esa caja
    List<Venta> ventas = ventaRepository.findByCajaIdCaja(idCaja);

    BigDecimal totalVentas = BigDecimal.ZERO;
    BigDecimal totalEfectivo = BigDecimal.ZERO;
    BigDecimal totalTransferencia = BigDecimal.ZERO;

   for (Venta v : ventas) {
    // totalVentas = totalVentas + v.getTotal()
    totalVentas = totalVentas.add(v.getTotal());

    if (v.getMetodoPago().equals(MetodoPago.EFECTIVO)) {
        totalEfectivo = totalEfectivo.add(v.getTotal());
    } else {
        totalTransferencia = totalTransferencia.add(v.getTotal());
    }
}


    // Llenar datos de cierre AUTOMÁTICOS
    caja.setFechaCierre(LocalDateTime.now());
    caja.setTotalVentas(totalVentas);
    caja.setTotalEfectivo(totalEfectivo);
    caja.setTotalTransferencia(totalTransferencia);

    // No hay conteo real, entonces son iguales
    caja.setEfectivoReal(totalEfectivo);
    caja.setTransferenciaReal(totalTransferencia);

    caja.setEstadoCaja(EstadoCaja.CERRADA);

    cajaRepository.save(caja);

    try {

        Usuario administrador = usuarioAuthService.getUsuarioLogueado();

        String descripcion =
                "Caja #" + caja.getIdCaja() + " cerrada de forma forzada.\n" +
                "Administrador: " + administrador.getNombreCompleto() + ".\n" +
                "Cajero: " + caja.getUsuario().getNombreCompleto() + ".\n" +
                "Total ventas: $" + caja.getTotalVentas().setScale(0, RoundingMode.HALF_UP) + ".";

        auditoriaService.registrar(
                administrador,
                "CAJA",
                "CIERRE FORZADO",
                descripcion
        );

    } catch (Exception e) {
        System.out.println("Error auditoría caja: " + e.getMessage());
    }
}


}
