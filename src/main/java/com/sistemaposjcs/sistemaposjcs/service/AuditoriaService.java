package com.sistemaposjcs.sistemaposjcs.service;

import com.sistemaposjcs.sistemaposjcs.dto.AuditoriaDTO;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.dto.UsuarioAuditoriaDTO;
import com.sistemaposjcs.sistemaposjcs.model.Auditoria;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario;
import com.sistemaposjcs.sistemaposjcs.repository.AuditoriaRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import org.springframework.data.core.TypedPropertyPath;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    // REGISTRAR AUDITORIA
    public void registrar(Usuario usuario, String modulo, String accion, String descripcion) {

        if (usuario == null) {
            throw new RuntimeException("Usuario no puede ser null en auditoría");
        }

        Auditoria auditoria = new Auditoria();
        auditoria.setUsuario(usuario);
        auditoria.setModulo(modulo);
        auditoria.setAccion(accion);
        auditoria.setDescripcion(descripcion);

        auditoriaRepository.save(auditoria);
    }

    // LISTAR DTO 
    public List<AuditoriaDTO> listar() {
        return auditoriaRepository.findAll(Sort.by(TypedPropertyPath.of(Auditoria::getId)).descending())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private AuditoriaDTO toDTO(Auditoria auditoria) {

        Usuario usuario = auditoria.getUsuario();

        UsuarioAuditoriaDTO usuarioDTO = new UsuarioAuditoriaDTO(
                usuario.getIdUsuario(),
                usuario.getUsername(),
                usuario.getNombreCompleto()
        );

        return new AuditoriaDTO(
                auditoria.getId(),
                auditoria.getFecha(),
                usuarioDTO,
                auditoria.getModulo(),
                auditoria.getAccion(),
                auditoria.getDescripcion()
        );
    }
    
    //Paginacion
    public Page<AuditoriaDTO> listarPaginado(
            Pageable pageable,
            String search,
            String usuario,
            String modulo,
            String accion,
            String fechaInicio,
            String fechaFin
    ) {

        return auditoriaRepository.findAll(
                        buildSpec(search, usuario, modulo, accion, fechaInicio, fechaFin),
                        pageable
                )
                .map(this::toDTO);
    }

    public Specification<Auditoria> buildSpec(
            String search,
            String usuario,
            String modulo,
            String accion,
            String fechaInicio,
            String fechaFin
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Auditoria, Usuario> usuarioJoin = root.join("usuario", JoinType.LEFT);

            String searchValue = normalizar(search);
            if (searchValue != null) {
                String likeValue = "%" + searchValue + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(usuarioJoin.get("username")), likeValue),
                        criteriaBuilder.like(criteriaBuilder.lower(usuarioJoin.get("primerNombre")), likeValue),
                        criteriaBuilder.like(criteriaBuilder.lower(usuarioJoin.get("segundoNombre")), likeValue),
                        criteriaBuilder.like(criteriaBuilder.lower(usuarioJoin.get("primerApellido")), likeValue),
                        criteriaBuilder.like(criteriaBuilder.lower(usuarioJoin.get("segundoApellido")), likeValue),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("modulo")), likeValue),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("accion")), likeValue),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("descripcion")), likeValue)
                ));
            }

            String usuarioValue = normalizar(usuario);
            if (usuarioValue != null) {
                String likeValue = "%" + usuarioValue + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(usuarioJoin.get("username")), likeValue),
                        criteriaBuilder.like(criteriaBuilder.lower(usuarioJoin.get("primerNombre")), likeValue),
                        criteriaBuilder.like(criteriaBuilder.lower(usuarioJoin.get("segundoNombre")), likeValue),
                        criteriaBuilder.like(criteriaBuilder.lower(usuarioJoin.get("primerApellido")), likeValue),
                        criteriaBuilder.like(criteriaBuilder.lower(usuarioJoin.get("segundoApellido")), likeValue)
                ));
            }

            String moduloValue = limpiar(modulo);
            if (moduloValue != null) {
                predicates.add(criteriaBuilder.equal(root.get("modulo"), moduloValue));
            }

            String accionValue = limpiar(accion);
            if (accionValue != null) {
                predicates.add(criteriaBuilder.equal(root.get("accion"), accionValue));
            }

            LocalDateTime desde = parseFechaInicio(fechaInicio);
            if (desde != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("fecha"), desde));
            }

            LocalDateTime hasta = parseFechaFin(fechaFin);
            if (hasta != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("fecha"), hasta));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private String limpiar(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    private String normalizar(String value) {
        String limpio = limpiar(value);
        return limpio == null ? null : limpio.toLowerCase();
    }

    private LocalDateTime parseFechaInicio(String value) {
        String limpio = limpiar(value);
        return limpio == null ? null : LocalDate.parse(limpio).atStartOfDay();
    }

    private LocalDateTime parseFechaFin(String value) {
        String limpio = limpiar(value);
        return limpio == null ? null : LocalDate.parse(limpio).atTime(23, 59, 59, 999999999);
    }
}
