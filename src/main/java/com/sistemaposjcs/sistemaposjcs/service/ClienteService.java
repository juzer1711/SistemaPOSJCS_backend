package com.sistemaposjcs.sistemaposjcs.service;

import com.sistemaposjcs.sistemaposjcs.model.Cliente;
import com.sistemaposjcs.sistemaposjcs.repository.ClienteRepository;


import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario;
import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.service.UsuarioAuthService;

import jakarta.persistence.criteria.Predicate;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import jakarta.persistence.criteria.Expression;
import static com.sistemaposjcs.sistemaposjcs.specification.SpecificationUtils.nombreCompleto;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioAuthService usuarioAuthService;
    private final AuditoriaService auditoriaService;

    @Autowired
    private Validator validator;

    public ClienteService(
            ClienteRepository clienteRepository,
            AuditoriaService auditoriaService,
            UsuarioAuthService usuarioAuthService
    ) {
        this.clienteRepository = clienteRepository;
        this.auditoriaService = auditoriaService;
        this.usuarioAuthService = usuarioAuthService;
    }

    // Obtener cliente por ID
    public Cliente getClienteById(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    }

    // Paginación activos
    public Page<Cliente> getActiveClientes(Pageable pageable) {
        return clienteRepository.findByEstadoTrue(pageable);
    }

    public Page<Cliente> getInactiveClientes(Pageable pageable) {
        return clienteRepository.findByEstadoFalse(pageable);
    }

    public Cliente createCliente(Cliente cliente) {

        if (cliente.getEstado() == null) {
            cliente.setEstado(true);
        }

        Cliente clienteGuardado = clienteRepository.save(cliente);

        try {

            Usuario usuario = usuarioAuthService.getUsuarioLogueado();

            String descripcion =
                    "Cliente creado.\n" +
                    "Nombre: " + clienteGuardado.getNombreCompleto() + ".\n" +
                    "Documento: " + clienteGuardado.getDocumento() + ".";

            auditoriaService.registrar(
                    usuario,
                    "CLIENTE",
                    "CREAR",
                    descripcion
            );

        } catch (Exception e) {
            System.out.println("Error auditoría cliente: " + e.getMessage());
        }

        return clienteGuardado;
    }

    // Actualizar cliente
    public Cliente updateCliente(Long id, Cliente data) {

        Cliente c = getClienteById(id);

        StringBuilder cambios = new StringBuilder();

        if (!java.util.Objects.equals(c.getPrimerNombre(), data.getPrimerNombre())) {
            cambios.append("Primer nombre: '").append(c.getPrimerNombre())
                    .append("' → '").append(data.getPrimerNombre()).append("', ");
        }

        if (!java.util.Objects.equals(c.getSegundoNombre(), data.getSegundoNombre())) {
            cambios.append("Segundo nombre: '").append(c.getSegundoNombre())
                    .append("' → '").append(data.getSegundoNombre()).append("', ");
        }

        if (!java.util.Objects.equals(c.getPrimerApellido(), data.getPrimerApellido())) {
            cambios.append("Primer apellido: '").append(c.getPrimerApellido())
                    .append("' → '").append(data.getPrimerApellido()).append("', ");
        }

        if (!java.util.Objects.equals(c.getSegundoApellido(), data.getSegundoApellido())) {
            cambios.append("Segundo apellido: '").append(c.getSegundoApellido())
                    .append("' → '").append(data.getSegundoApellido()).append("', ");
        }

        if (!java.util.Objects.equals(c.getDocumento(), data.getDocumento())) {
            cambios.append("Documento: '").append(c.getDocumento())
                    .append("' → '").append(data.getDocumento()).append("', ");
        }

        if (!java.util.Objects.equals(c.getTelefono(), data.getTelefono())) {
            cambios.append("Teléfono: '").append(c.getTelefono())
                    .append("' → '").append(data.getTelefono()).append("', ");
        }

        if (!java.util.Objects.equals(c.getEmail(), data.getEmail())) {
            cambios.append("Email actualizado, ");
        }

        if (!java.util.Objects.equals(c.getDireccion(), data.getDireccion())) {
            cambios.append("Dirección actualizada, ");
        }

        if (!java.util.Objects.equals(c.getTipoCliente(), data.getTipoCliente())) {
            cambios.append("Tipo cliente: '").append(c.getTipoCliente())
                    .append("' → '").append(data.getTipoCliente()).append("', ");
        }

        if (!java.util.Objects.equals(c.getTipoDocumento(), data.getTipoDocumento())) {
            cambios.append("Tipo documento: '").append(c.getTipoDocumento())
                    .append("' → '").append(data.getTipoDocumento()).append("', ");
        }

        if (!java.util.Objects.equals(c.getRazonSocial(), data.getRazonSocial())) {
            cambios.append("Razón social actualizada, ");
        }

        if (!java.util.Objects.equals(c.getIdentificadorNit(), data.getIdentificadorNit())) {
            cambios.append("NIT actualizado, ");
        }

        c.setTipoCliente(data.getTipoCliente());

        c.setPrimerNombre(data.getPrimerNombre());
        c.setSegundoNombre(data.getSegundoNombre());
        c.setPrimerApellido(data.getPrimerApellido());
        c.setSegundoApellido(data.getSegundoApellido());

        c.setRazonSocial(data.getRazonSocial());
        c.setIdentificadorNit(data.getIdentificadorNit());

        c.setTipoDocumento(data.getTipoDocumento());
        c.setDocumento(data.getDocumento());
        c.setEmail(data.getEmail());
        c.setTelefono(data.getTelefono());
        c.setDireccion(data.getDireccion());

        Set<ConstraintViolation<Cliente>> violations = validator.validate(c);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        Cliente clienteActualizado = clienteRepository.save(c);

        try {

            Usuario usuario = usuarioAuthService.getUsuarioLogueado();

            String descripcion =
                    "Cliente '" + clienteActualizado.getNombreCompleto() + "'.\n" +
                    (cambios.length() > 2
                            ? cambios.substring(0, cambios.length() - 2)
                            : "Sin cambios relevantes.");

            auditoriaService.registrar(
                    usuario,
                    "CLIENTE",
                    "ACTUALIZAR",
                    descripcion
            );

        } catch (Exception e) {
            System.out.println("Error auditoría cliente: " + e.getMessage());
        }

        return clienteActualizado;
    }

    public void desactivarCliente(Long id) {

        Cliente c = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        c.setEstado(false);

        clienteRepository.save(c);

        try {

            Usuario usuario = usuarioAuthService.getUsuarioLogueado();

            String descripcion =
                    "Cliente desactivado.\n" +
                    "Nombre: " + c.getNombreCompleto() + ".\n" +
                    "Documento: " + c.getDocumento() + ".";

            auditoriaService.registrar(
                    usuario,
                    "CLIENTE",
                    "DESACTIVAR",
                    descripcion
            );

        } catch (Exception e) {
            System.out.println("Error auditoría cliente: " + e.getMessage());
        }
    }

    public Cliente activarCliente(Long id) {

        Cliente c = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        c.setEstado(true);

        Cliente clienteActivado = clienteRepository.save(c);

        try {

            Usuario usuario = usuarioAuthService.getUsuarioLogueado();

            String descripcion =
                    "Cliente reactivado.\n" +
                    "Nombre: " + clienteActivado.getNombreCompleto() + ".\n" +
                    "Documento: " + clienteActivado.getDocumento() + ".";

            auditoriaService.registrar(
                    usuario,
                    "CLIENTE",
                    "ACTIVAR",
                    descripcion
            );

        } catch (Exception e) {
            System.out.println("Error auditoría cliente: " + e.getMessage());
        }

        return clienteActivado;
    }

    public Page<Cliente> searchClientes(
        Pageable pageable,
        String search,
        String tipoCliente,
        String tipoDocumento,
        Boolean estado
    ) {
        Specification<Cliente> spec = buildSpec(search, tipoCliente, tipoDocumento, estado);
        return clienteRepository.findAll(spec, pageable);
    }

    public Specification<Cliente> buildSpec(
        String search,
        String tipoCliente,
        String tipoDocumento,
        Boolean estado
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // búsqueda global
            if (search != null && !search.isEmpty()) {
                String like = "%" + search.toLowerCase().replace(" ", "") + "%";
                Expression<String> nombre = nombreCompleto(cb, root);

                predicates.add(cb.or(
                    cb.like(nombre, like),
                    cb.like(cb.lower(root.get("razonSocial")), like),
                    cb.like(cb.lower(root.get("documento")), like)
                ));
            }

            // tipo cliente
            if (tipoCliente != null && !tipoCliente.isEmpty()) {
                predicates.add(cb.equal(root.get("tipoCliente"), tipoCliente));
            }

            // tipo documento
            if (tipoDocumento != null && !tipoDocumento.isEmpty()) {
                predicates.add(cb.equal(root.get("tipoDocumento"), tipoDocumento));
            }

            // estado
            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
