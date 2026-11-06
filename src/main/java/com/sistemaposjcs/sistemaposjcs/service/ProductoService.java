package com.sistemaposjcs.sistemaposjcs.service;

import com.sistemaposjcs.sistemaposjcs.model.Producto;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario;
import com.sistemaposjcs.sistemaposjcs.model.Categoria;
import com.sistemaposjcs.sistemaposjcs.repository.CategoriaRepository;
import com.sistemaposjcs.sistemaposjcs.repository.ProductoRepository;
import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.service.UsuarioAuthService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Join;
import java.util.ArrayList;
import java.util.List;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ProductoService {
    private static final Logger log = LoggerFactory.getLogger(ProductoService.class);

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final AuditoriaService auditoriaService;
    private final UsuarioAuthService usuarioAuthService;
    
    public ProductoService(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            AuditoriaService auditoriaService,
            UsuarioAuthService usuarioAuthService
    ) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.auditoriaService = auditoriaService;
        this.usuarioAuthService = usuarioAuthService;
    }

    // Obtener un producto por ID
    public Producto getProductoById(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    // Listar todos los usuarios inactivos
    public Page<Producto> getActiveProductos(Pageable pageable) {
        return productoRepository.findByEstadoTrue(pageable);
    }

    public Page<Producto> getInactiveProductos(Pageable pageable) {
        return productoRepository.findByEstadoFalse(pageable);
    }

    // Crear producto
    public Producto createProducto(Producto producto) {
        if (producto.getEstado() == null) {
            producto.setEstado(true);
        }
        if (producto.getStockActual() == null) {
            producto.setStockActual(0);
        }
        if (producto.getStockMinimo() == null) {
            producto.setStockMinimo(0);
        }

        // Obtener valor numérico del IVA
        BigDecimal ivaValue = producto.getIva().getValue();

        // precio sin IVA = precioFinal / (1 + iva)
        BigDecimal divisor = BigDecimal.ONE.add(ivaValue);
        BigDecimal precioSinIva = producto.getPrecioventa().divide(divisor, 2, RoundingMode.HALF_UP);

        producto.setPrecioSinIva(precioSinIva);
        // Reemplazar categoria recibida con la REAL de la BD
        if (producto.getCategoria() != null && producto.getCategoria().getId() != null) {
            Categoria categoriaReal = categoriaRepository.findById(producto.getCategoria().getId())
                    .orElseThrow(() -> new RuntimeException("Categoria no encontrado"));
            producto.setCategoria(categoriaReal);
        }

        Producto productoGuardado = productoRepository.save(producto);
        
        try {
            Usuario usuario = usuarioAuthService.getUsuarioLogueado();

            auditoriaService.registrar(
                usuario,
                "PRODUCTO",
                "CREAR",
                "Producto creado: " + productoGuardado.getNombre()
            );
        } catch (Exception e) {
            log.warn("Error auditoría: " + e.getMessage());
        }

        return productoGuardado;
    }

    // Actualizar producto
    public Producto updateProducto(Long id, Producto productoDetails) {

        Producto producto = getProductoById(id);

        // ===== Guardar valores anteriores =====
        String nombreAnterior = producto.getNombre();
        String codigoAnterior = producto.getCodigoBarras();
        String descripcionAnterior = producto.getDescripcion();
        BigDecimal costoAnterior = producto.getCosto();
        BigDecimal precioAnterior = producto.getPrecioventa();
        Categoria categoriaAnterior = producto.getCategoria();
        var ivaAnterior = producto.getIva();

        // ===== Actualizar datos =====
        producto.setNombre(productoDetails.getNombre());
        producto.setCodigoBarras(productoDetails.getCodigoBarras());
        producto.setDescripcion(productoDetails.getDescripcion());
        producto.setCosto(productoDetails.getCosto());
        producto.setPrecioventa(productoDetails.getPrecioventa());
        producto.setIva(productoDetails.getIva());

        BigDecimal ivaValue = producto.getIva().getValue();
        BigDecimal divisor = BigDecimal.ONE.add(ivaValue);
        BigDecimal precioSinIva = producto.getPrecioventa().divide(divisor, 2, RoundingMode.HALF_UP);

        producto.setPrecioSinIva(precioSinIva);

        if (productoDetails.getCategoria() != null && productoDetails.getCategoria().getId() != null) {
            Categoria nuevaCategoria = categoriaRepository.findById(productoDetails.getCategoria().getId())
                    .orElseThrow(() -> new RuntimeException("Categoria no encontrada"));
            producto.setCategoria(nuevaCategoria);
        }

        Producto productoActualizado = productoRepository.save(producto);

        try {
            Usuario usuario = usuarioAuthService.getUsuarioLogueado();

            StringBuilder cambios = new StringBuilder();

            if (!nombreAnterior.equals(productoActualizado.getNombre())) {
                cambios.append("Nombre: '")
                    .append(nombreAnterior)
                    .append("' → '")
                    .append(productoActualizado.getNombre())
                    .append("', ");
            }

            if (!codigoAnterior.equals(productoActualizado.getCodigoBarras())) {
                cambios.append("Código: '")
                    .append(codigoAnterior)
                    .append("' → '")
                    .append(productoActualizado.getCodigoBarras())
                    .append("', ");
            }

            if (!descripcionAnterior.equals(productoActualizado.getDescripcion())) {
                cambios.append("Descripción modificada, ");
            }

            if (costoAnterior.compareTo(productoActualizado.getCosto()) != 0) {
                cambios.append("Costo: ")
                    .append(costoAnterior)
                    .append(" → ")
                    .append(productoActualizado.getCosto())
                    .append(", ");
            }

            if (precioAnterior.compareTo(productoActualizado.getPrecioventa()) != 0) {
                cambios.append("Precio: ")
                    .append(precioAnterior)
                    .append(" → ")
                    .append(productoActualizado.getPrecioventa())
                    .append(", ");
            }

            if (!ivaAnterior.equals(productoActualizado.getIva())) {
                cambios.append("IVA: ")
                    .append(ivaAnterior)
                    .append(" → ")
                    .append(productoActualizado.getIva())
                    .append(", ");
            }

            if (!categoriaAnterior.getId().equals(productoActualizado.getCategoria().getId())) {
                cambios.append("Categoría: '")
                    .append(categoriaAnterior.getNombre())
                    .append("' → '")
                    .append(productoActualizado.getCategoria().getNombre())
                    .append("', ");
            }

            String descripcion = cambios.toString();

            if (descripcion.endsWith(", ")) {
                descripcion = descripcion.substring(0, descripcion.length() - 2);
            }

            // Si no hubo cambios (por si acaso)
            if (descripcion.isEmpty()) {
                descripcion = "No se detectaron cambios";
            } else {
                descripcion = "Producto '" + productoActualizado.getNombre() + "': " + descripcion;
            }

            auditoriaService.registrar(
                    usuario,
                    "PRODUCTO",
                    "ACTUALIZAR",
                    descripcion
            );

        } catch (Exception e) {
            log.warn("No se pudo registrar auditoría update producto: {}", e.getMessage());
        }

        return productoActualizado;
    }

    //DESACTIVAR PRODUCTO
    public void desactivarProducto(Long id) {

        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        p.setEstado(false);

        productoRepository.save(p);

        try {
            Usuario usuario = usuarioAuthService.getUsuarioLogueado();

            auditoriaService.registrar(
                    usuario,
                    "PRODUCTO",
                    "DESACTIVAR",
                    "Producto desactivado: " + p.getNombre()
            );
        } catch (Exception e) {
            log.warn("No se pudo registrar auditoría desactivar producto: {}", e.getMessage());
        }
    }

    //ACTIVAR PRODUCTO
    public Producto activarProducto(Long id) {

        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        p.setEstado(true);

        Producto producto = productoRepository.save(p);

        try {
            Usuario usuario = usuarioAuthService.getUsuarioLogueado();

            auditoriaService.registrar(
                    usuario,
                    "PRODUCTO",
                    "ACTIVAR",
                    "Producto activado: " + producto.getNombre()
            );

        } catch (Exception e) {
            log.warn("No se pudo registrar auditoría activar producto: {}", e.getMessage());
        }

        return producto;
    }

    //ACTUALIZAR STOCK MINIMO
    public Producto actualizarStockMinimo(Long idProducto, Integer stockMinimo) {

        if (stockMinimo == null || stockMinimo < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo");
        }

        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Guardamos el valor anterior
        Integer stockAnterior = producto.getStockMinimo();

        producto.setStockMinimo(stockMinimo);

        Producto productoActualizado = productoRepository.save(producto);

        try {
            Usuario usuario = usuarioAuthService.getUsuarioLogueado();

            auditoriaService.registrar(
                    usuario,
                    "PRODUCTO",
                    "ACTUALIZAR STOCK MÍNIMO",
                    "Stock mínimo de '" + productoActualizado.getNombre()
                            + "' cambiado de " + stockAnterior
                            + " a " + stockMinimo
            );

        } catch (Exception e) {
            log.warn("No se pudo registrar auditoría stock mínimo: {}", e.getMessage());
        }

        return productoActualizado;
    }

    public Page<Producto> searchProductos(
        Pageable pageable,
        String search,
        Long categoria,
        Boolean estado
    ) {
        Specification<Producto> spec = buildSpec(search, categoria, estado);
        return productoRepository.findAll(spec, pageable);
    }

    public Specification<Producto> buildSpec(
        String search,
        Long categoria,
        Boolean estado
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // búsqueda global
            if (search != null && !search.isEmpty()) {
                String searchLower = "%" + search.toLowerCase() + "%";

                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("nombre")), searchLower),
                    cb.like(cb.lower(root.get("descripcion")), searchLower),
                    cb.like(cb.lower(root.get("codigoBarras")), searchLower)
                ));
            }

            // categoría
            if (categoria != null) {
                Join<Object, Object> categoriaJoin = root.join("categoria");
                predicates.add(cb.equal(categoriaJoin.get("id"), categoria));
            }

            // estado
            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
