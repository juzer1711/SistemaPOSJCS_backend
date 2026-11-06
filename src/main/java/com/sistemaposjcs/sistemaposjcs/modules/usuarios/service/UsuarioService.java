package com.sistemaposjcs.sistemaposjcs.modules.usuarios.service;

import com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.dto.UsuarioRequest;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.dto.UsuarioResponse;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.dto.UsuarioVista;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.repository.UsuarioRepository;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.service.ReglaService;
import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.service.UsuarioAuthService;
import com.sistemaposjcs.sistemaposjcs.model.Enum.TipoDocumento;
import com.sistemaposjcs.sistemaposjcs.service.AuditoriaService;
import jakarta.persistence.criteria.Predicate;
import java.util.*;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static com.sistemaposjcs.sistemaposjcs.specification.SpecificationUtils.nombreCompleto;

@Service
@Transactional(readOnly = true)
public class UsuarioService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UsuarioService.class);
    private static final Set<String> SORT_FIELDS = Set.of("idUsuario", "username", "primerNombre", "primerApellido",
            "nombreCompleto", "documento", "email", "telefono", "estado", "tipoDocumento");
    private final UsuarioRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;
    private final UsuarioAuthService usuarioAuthService;
    private final ReglaService reglaService;

    public UsuarioService(UsuarioRepository userRepository, AuditoriaService auditoriaService,
            UsuarioAuthService usuarioAuthService, PasswordEncoder passwordEncoder, ReglaService reglaService) {
        this.userRepository = userRepository;
        this.auditoriaService = auditoriaService;
        this.usuarioAuthService = usuarioAuthService;
        this.passwordEncoder = passwordEncoder;
        this.reglaService = reglaService;
    }

    public Usuario getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    public Page<UsuarioResponse> getActiveUsers(Pageable pageable) { return searchUsuarios(pageable, null, true, null); }
    public Page<UsuarioResponse> getInactiveUsers(Pageable pageable) { return searchUsuarios(pageable, null, false, null); }

    public Page<UsuarioResponse> searchUsuarios(Pageable pageable, String search, Boolean estado, TipoDocumento tipoDocumento) {
        Pageable normalized = limitarPageable(pageable);
        return userRepository.findBy(buildSpec(search, estado, tipoDocumento),
                query -> query.as(UsuarioVista.class).page(normalized)).map(UsuarioResponse::from);
    }

    /** Exportación por clave creciente: no carga todas las entidades ni ejecuta COUNT por cada lote. */
    public void recorrerUsuarios(String search, Boolean estado, TipoDocumento tipoDocumento, Consumer<UsuarioResponse> consumer) {
        Specification<Usuario> spec = buildSpec(search, estado, tipoDocumento);
        KeysetScrollPosition position = ScrollPosition.keyset();
        Window<UsuarioVista> window;
        do {
            KeysetScrollPosition currentPosition = position;
            window = userRepository.findBy(spec, query -> query.as(UsuarioVista.class)
                    .sortBy(Sort.by("idUsuario")).limit(500).scroll(currentPosition));
            window.forEach(value -> consumer.accept(UsuarioResponse.from(value)));
            if (!window.isEmpty()) position = (KeysetScrollPosition) window.positionAt(window.size() - 1);
        } while (window.hasNext());
    }

    @Transactional
    public Usuario createUser(UsuarioRequest request) {
        validarUnicidad(request, null);
        Usuario operador = usuarioAuthService.getUsuarioLogueado();
        Usuario usuario = new Usuario();
        aplicarDatos(usuario, request);
        usuario.setPassword(encodePassword(request.getPassword()));
        usuario.setEstado(true);
        Usuario saved = userRepository.save(usuario);
        reglaService.crearReglasFalseParaUsuario(saved);
        auditar(operador, "CREAR", "Usuario creado.\nNombre: " + saved.getNombreCompleto() + ".\nUsuario: " + saved.getUsername() + ".");
        return saved;
    }

    @Transactional
    public Usuario updateUser(Long id, UsuarioRequest request) {
        Usuario usuario = obtenerParaActualizar(id);
        if (!usuario.getDocumento().equals(request.getDocumento())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El documento del usuario no se puede modificar");
        }
        validarUnicidad(request, id);
        Usuario operador = usuarioAuthService.getUsuarioLogueado();
        UsuarioResponse before = UsuarioResponse.from(usuario);
        aplicarDatos(usuario, request);
        boolean changePassword = request.getPassword() != null && !request.getPassword().isBlank();
        if (changePassword) usuario.setPassword(encodePassword(request.getPassword()));
        Usuario saved = userRepository.save(usuario);
        List<String> cambios = new ArrayList<>();
        comparar(cambios, "Usuario", before.username(), saved.getUsername());
        comparar(cambios, "Primer nombre", before.primerNombre(), saved.getPrimerNombre());
        comparar(cambios, "Segundo nombre", before.segundoNombre(), saved.getSegundoNombre());
        comparar(cambios, "Primer apellido", before.primerApellido(), saved.getPrimerApellido());
        comparar(cambios, "Segundo apellido", before.segundoApellido(), saved.getSegundoApellido());
        comparar(cambios, "Tipo de documento", before.tipoDocumento(), saved.getTipoDocumento());
        comparar(cambios, "Email", before.email(), saved.getEmail());
        comparar(cambios, "Teléfono", before.telefono(), saved.getTelefono());
        if (changePassword) cambios.add("Contraseña actualizada.");
        if (!cambios.isEmpty()) auditar(operador, "ACTUALIZAR", "Usuario '" + saved.getNombreCompleto() + "'.\n" + String.join("\n", cambios));
        return saved;
    }

    @Transactional
    public void desactivarUsuario(Long id) { cambiarEstado(id, false); }

    @Transactional
    public Usuario activarUsuario(Long id) { return cambiarEstado(id, true); }

    private Usuario obtenerParaActualizar(Long id) {
        return userRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private Usuario cambiarEstado(Long id, boolean active) {
        Usuario usuario = obtenerParaActualizar(id);
        Usuario operador = usuarioAuthService.getUsuarioLogueado();
        if (Objects.equals(usuario.getEstado(), active)) return usuario;
        usuario.setEstado(active);
        Usuario saved = userRepository.save(usuario);
        auditar(operador, active ? "ACTIVAR" : "DESACTIVAR",
                "Usuario " + (active ? "reactivado" : "desactivado") + ".\nNombre: " + saved.getNombreCompleto() + ".\nUsuario: " + saved.getUsername() + ".");
        return saved;
    }

    private String encodePassword(String password) {
        if (password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña no puede superar 72 bytes UTF-8");
        return passwordEncoder.encode(password);
    }

    private void auditar(Usuario operador, String accion, String descripcion) {
        try {
            auditoriaService.registrar(operador, "USUARIO", accion, descripcion);
        } catch (RuntimeException error) {
            // Mantiene el manejo existente. Un fallo SQL que marque rollback revierte la transacción completa.
            LOGGER.warn("No fue posible registrar la auditoría de usuarios ({})", accion);
        }
    }

    private void comparar(List<String> cambios, String campo, Object before, Object after) {
        if (!Objects.equals(before, after)) cambios.add(campo + ": '" + Objects.toString(before, "") + "' → '" + Objects.toString(after, "") + "'");
    }

    private void aplicarDatos(Usuario usuario, UsuarioRequest request) {
        usuario.setUsername(request.getUsername().trim());
        usuario.setPrimerNombre(request.getPrimerNombre().trim());
        usuario.setSegundoNombre(normalizarOpcional(request.getSegundoNombre()));
        usuario.setPrimerApellido(request.getPrimerApellido().trim());
        usuario.setSegundoApellido(normalizarOpcional(request.getSegundoApellido()));
        usuario.setTipoDocumento(request.getTipoDocumento());
        usuario.setDocumento(request.getDocumento().trim());
        usuario.setEmail(request.getEmail().trim());
        usuario.setTelefono(request.getTelefono().trim());
    }

    private String normalizarOpcional(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    private void validarUnicidad(UsuarioRequest request, Long id) {
        boolean username = id == null ? userRepository.existsByUsername(request.getUsername().trim())
                : userRepository.existsByUsernameAndIdUsuarioNot(request.getUsername().trim(), id);
        boolean documento = id == null ? userRepository.existsByDocumento(request.getDocumento().trim())
                : userRepository.existsByDocumentoAndIdUsuarioNot(request.getDocumento().trim(), id);
        boolean email = id == null ? userRepository.existsByEmail(request.getEmail().trim())
                : userRepository.existsByEmailAndIdUsuarioNot(request.getEmail().trim(), id);
        if (username) throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de usuario ya existe");
        if (documento) throw new ResponseStatusException(HttpStatus.CONFLICT, "El documento ya existe");
        if (email) throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya existe");
    }

    public Pageable limitarPageable(Pageable pageable) {
        List<Sort.Order> orders = new ArrayList<>();
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by("primerApellido", "primerNombre");
        for (Sort.Order order : sort) {
            if (!SORT_FIELDS.contains(order.getProperty())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo de ordenamiento no permitido: " + order.getProperty());
            List<String> properties = order.getProperty().equals("nombreCompleto")
                    ? List.of("primerNombre", "segundoNombre", "primerApellido", "segundoApellido") : List.of(order.getProperty());
            for (String property : properties) {
                Sort.Order safe = new Sort.Order(order.getDirection(), property);
                orders.add(Set.of("idUsuario", "estado", "tipoDocumento").contains(property) ? safe : safe.ignoreCase());
            }
        }
        if (orders.stream().noneMatch(order -> order.getProperty().equals("idUsuario"))) orders.add(Sort.Order.asc("idUsuario"));
        return PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), 100), Sort.by(orders));
    }

    public Specification<Usuario> buildSpec(String search, Boolean estado, TipoDocumento tipoDocumento) {
        String value = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        if (value.length() > 150) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La búsqueda no puede superar 150 caracteres");
        String pattern = "%" + escapar(value) + "%";
        String namePattern = "%" + escapar(value.replace(" ", "")) + "%";
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!value.isBlank()) predicates.add(cb.or(
                    cb.like(nombreCompleto(cb, root), namePattern, '\\'),
                    cb.like(cb.lower(root.get("username")), pattern, '\\'),
                    cb.like(cb.lower(root.get("documento")), pattern, '\\'),
                    cb.like(cb.lower(root.get("email")), pattern, '\\')));
            if (estado != null) predicates.add(cb.equal(root.get("estado"), estado));
            if (tipoDocumento != null) predicates.add(cb.equal(root.get("tipoDocumento"), tipoDocumento));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private String escapar(String value) { return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_"); }
}