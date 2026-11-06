package com.sistemaposjcs.sistemaposjcs.modules.reglas.service;

import com.sistemaposjcs.sistemaposjcs.modules.modulos.model.Modulo;
import com.sistemaposjcs.sistemaposjcs.modules.modulos.repository.ModuloRepository;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.dto.ReglaRequest;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.dto.ReglaResponse;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.model.Regla;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.repository.ReglaRepository;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.repository.UsuarioRepository;
import jakarta.persistence.criteria.Predicate;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.dto.MatrizPermisoResponse;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.dto.ReglaUsuarioOpcion;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.dto.ReglaModuloOpcion;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.dto.PermisoCambioRequest;
import org.springframework.data.domain.PageRequest;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReglaService {

    private final ReglaRepository reglaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModuloRepository moduloRepository;

    public ReglaService(
            ReglaRepository reglaRepository,
            UsuarioRepository usuarioRepository,
            ModuloRepository moduloRepository
    ) {
        this.reglaRepository = reglaRepository;
        this.usuarioRepository = usuarioRepository;
        this.moduloRepository = moduloRepository;
    }

    @Transactional(readOnly = true)
    public Page<ReglaResponse> listar(Pageable pageable, Long usuarioId, Long moduloId) {
        Specification<Regla> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (usuarioId != null) {
                predicates.add(criteriaBuilder.equal(root.get("usuario").get("idUsuario"), usuarioId));
            }
            if (moduloId != null) {
                predicates.add(criteriaBuilder.equal(root.get("modulo").get("idModulo"), moduloId));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };

        return reglaRepository.findAll(specification, pageable).map(ReglaResponse::from);
    }

    @Transactional(readOnly = true)
    public ReglaResponse obtenerPorId(Long idRegla) {
        return ReglaResponse.from(obtenerRegla(idRegla));
    }

    @Transactional
    public ReglaResponse crear(ReglaRequest request) {
        Regla regla = aplicarDatos(new Regla(), request);
        validarUnicidad(regla, null);
        return ReglaResponse.from(reglaRepository.save(regla));
    }

    @Transactional
    public ReglaResponse actualizar(Long idRegla, ReglaRequest request) {
        Regla regla = obtenerRegla(idRegla);
        aplicarDatos(regla, request);
        validarUnicidad(regla, idRegla);
        return ReglaResponse.from(reglaRepository.save(regla));
    }

    @Transactional
    public void eliminar(Long idRegla) {
        reglaRepository.delete(obtenerRegla(idRegla));
    }

    @Transactional
    public void crearReglasFalseParaModulo(Modulo modulo) {
        reglaRepository.crearFalseParaModulo(modulo.getIdModulo());
    }

    @Transactional
    public void crearReglasFalseParaUsuario(Usuario usuario) {
        reglaRepository.crearFalseParaUsuario(usuario.getIdUsuario());
    }

    @Transactional
    public void eliminarPorModulo(Long moduloId) {
        reglaRepository.deleteByModulo_IdModulo(moduloId);
    }

    @Transactional(readOnly = true)
    public Page<MatrizPermisoResponse> matriz(Long usuarioId, Long moduloId, Pageable pageable) {
        if (usuarioId == null || usuarioId <= 0 || !usuarioRepository.existsById(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        return reglaRepository.matriz(usuarioId, moduloId, paginacionMatriz(pageable));
    }

    @Transactional(readOnly = true)
    public Page<ReglaUsuarioOpcion> usuarios(String search, Pageable pageable) {
        return reglaRepository.usuarios(patronSelector(search), paginacionMatriz(pageable));
    }

    @Transactional(readOnly = true)
    public Page<ReglaModuloOpcion> modulos(String search, Pageable pageable) {
        return reglaRepository.modulos(patronSelector(search), paginacionMatriz(pageable));
    }

    @Transactional
    public void cambiarPermiso(PermisoCambioRequest request) {
        if (!usuarioRepository.existsById(request.usuarioId()) || !moduloRepository.existsById(request.moduloId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario o módulo no encontrado");
        }
        reglaRepository.asegurarRegla(request.usuarioId(), request.moduloId());
        if (reglaRepository.cambiarPermiso(request.usuarioId(), request.moduloId(), request.accion().name(), request.valor()) != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La regla cambió; vuelve a consultar la matriz");
        }
    }

    private Pageable paginacionMatriz(Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esta consulta utiliza un orden fijo por nombre e ID");
        }
        return PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), 100));
    }

    private String patronSelector(String search) {
        String value = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        if (value.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La búsqueda no puede superar 100 caracteres");
        }
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
    }

    private Regla obtenerRegla(Long idRegla) {
        return reglaRepository.findById(idRegla)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Regla no encontrada"));
    }

    private Regla aplicarDatos(Regla regla, ReglaRequest request) {
        regla.setUsuario(usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario no encontrado")));
        regla.setModulo(moduloRepository.findById(request.moduloId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Módulo no encontrado")));
        regla.setReglaConsulta(request.reglaConsulta());
        regla.setReglaInsercion(request.reglaInsercion());
        regla.setReglaActualizacion(request.reglaActualizacion());
        regla.setReglaReporte(request.reglaReporte());
        regla.setReglaDelete(request.reglaDelete());
        return regla;
    }

    private void validarUnicidad(Regla regla, Long idRegla) {
        Long usuarioId = regla.getUsuario().getIdUsuario();
        Long moduloId = regla.getModulo().getIdModulo();
        boolean duplicada = idRegla == null
                ? reglaRepository.existsByUsuario_IdUsuarioAndModulo_IdModulo(usuarioId, moduloId)
                : reglaRepository.existsByUsuario_IdUsuarioAndModulo_IdModuloAndIdReglaNot(
                        usuarioId, moduloId, idRegla
                );

        if (duplicada) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una regla para ese usuario y módulo");
        }
    }
}
