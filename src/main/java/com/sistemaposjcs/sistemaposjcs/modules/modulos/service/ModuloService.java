package com.sistemaposjcs.sistemaposjcs.modules.modulos.service;

import com.sistemaposjcs.sistemaposjcs.modules.modulos.dto.ModuloRequest;
import com.sistemaposjcs.sistemaposjcs.modules.modulos.model.Modulo;
import com.sistemaposjcs.sistemaposjcs.modules.modulos.repository.ModuloRepository;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.service.ReglaService;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.core.TypedPropertyPath;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ModuloService {

    private static final Set<String> CAMPOS_ORDENABLES = Set.of("idModulo", "codigoModulo", "nombreModulo", "zona");

    private final ModuloRepository moduloRepository;
    private final ReglaService reglaService;

    public ModuloService(ModuloRepository moduloRepository, ReglaService reglaService) {
        this.moduloRepository = moduloRepository;
        this.reglaService = reglaService;
    }

    public Page<Modulo> listar(Pageable pageable) {
        return moduloRepository.findAll(validarPaginacion(pageable));
    }

    public Modulo obtenerPorId(Long idModulo) {
        return moduloRepository.findById(idModulo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Módulo no encontrado"));
    }

    @Transactional
    public Modulo crear(ModuloRequest request) {
        validarCodigoUnico(request.codigoModulo());
        validarNombreUnico(request.nombreModulo(), null);
        Modulo modulo = moduloRepository.save(aplicarDatos(new Modulo(), request));
        reglaService.crearReglasFalseParaModulo(modulo);
        return modulo;
    }

    @Transactional
    public Modulo actualizar(Long idModulo, ModuloRequest request) {
        Modulo modulo = obtenerPorId(idModulo);
        if (!modulo.getCodigoModulo().equals(request.codigoModulo())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El código del módulo es inmutable"
            );
        }
        validarNombreUnico(request.nombreModulo(), idModulo);
        return moduloRepository.save(aplicarDatos(modulo, request));
    }

    @Transactional
    public void eliminar(Long idModulo) {
        Modulo modulo = obtenerPorId(idModulo);
        reglaService.eliminarPorModulo(idModulo);
        moduloRepository.delete(modulo);
    }

    public Page<Modulo> buscar(Pageable pageable, String nombreModulo, String zona) {
        String nombreNormalizado = normalizarFiltro(nombreModulo);
        String zonaNormalizada = normalizarFiltro(zona);

        Specification<Modulo> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (nombreNormalizado != null) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nombreModulo")),
                        patronPrefijo(nombreNormalizado),
                        '\\'
                ));
            }

            if (zonaNormalizada != null) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("zona")),
                        patronPrefijo(zonaNormalizada),
                        '\\'
                ));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };

        return moduloRepository.findAll(specification, validarPaginacion(pageable));
    }

    public boolean codigoDisponible(String codigoModulo) {
        if (codigoModulo == null || codigoModulo.length() > 50
                || !codigoModulo.matches("^[A-Z][A-Z0-9_]*$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código de módulo inválido");
        }
        return !moduloRepository.existsByCodigoModulo(codigoModulo);
    }

    private Pageable validarPaginacion(Pageable pageable) {
        List<Sort.Order> ordenes = new ArrayList<>();
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort()
                : Sort.by(TypedPropertyPath.of(Modulo::getNombreModulo));
        for (Sort.Order order : sort) {
            if (!CAMPOS_ORDENABLES.contains(order.getProperty())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Campo de ordenamiento no permitido: " + order.getProperty());
            }
            Sort.Order orden = new Sort.Order(order.getDirection(), order.getProperty());
            ordenes.add(order.getProperty().equals("idModulo") ? orden : orden.ignoreCase());
        }
        if (ordenes.stream().noneMatch(order -> order.getProperty().equals("idModulo"))) {
            ordenes.add(Sort.Order.asc(TypedPropertyPath.of(Modulo::getIdModulo)));
        }
        return PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), 100), Sort.by(ordenes));
    }

    private Modulo aplicarDatos(Modulo modulo, ModuloRequest request) {
        modulo.setCodigoModulo(request.codigoModulo());
        modulo.setArchivo(request.archivo().trim());
        modulo.setNombreModulo(request.nombreModulo().trim());
        modulo.setZona(request.zona().trim());
        modulo.setComentarios(normalizarOpcional(request.comentarios()));
        return modulo;
    }

    private void validarNombreUnico(String nombreModulo, Long idModulo) {
        String nombreNormalizado = nombreModulo.trim();
        boolean existe = idModulo == null
                ? moduloRepository.existsByNombreModuloIgnoreCase(nombreNormalizado)
                : moduloRepository.existsByNombreModuloIgnoreCaseAndIdModuloNot(nombreNormalizado, idModulo);

        if (existe) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un módulo con ese nombre");
        }
    }

    private void validarCodigoUnico(String codigoModulo) {
        if (moduloRepository.existsByCodigoModulo(codigoModulo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un módulo con ese código");
        }
    }

    private String normalizarFiltro(String value) {
        return value == null || value.isBlank() ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private String patronPrefijo(String value) {
        return value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_") + "%";
    }

    private String normalizarOpcional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
