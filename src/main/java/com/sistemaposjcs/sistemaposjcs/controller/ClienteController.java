package com.sistemaposjcs.sistemaposjcs.controller;

import com.sistemaposjcs.sistemaposjcs.dto.ClienteDTO;
import com.sistemaposjcs.sistemaposjcs.model.Cliente;
import com.sistemaposjcs.sistemaposjcs.service.ClienteService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/clientes")

public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // 1. ACTIVOS con paginación
    @GetMapping
    public Page<ClienteDTO> getActiveClientes(Pageable pageable) {
        return clienteService.getActiveClientes(pageable)
            .map(c -> mapToDTO(c));
    }

    // 2. INACTIVOS con paginación
    @GetMapping("/inactivos")
    public Page<ClienteDTO> getInactiveClientes(Pageable pageable) {
        return clienteService.getInactiveClientes(pageable)
            .map(c -> mapToDTO(c));
    }

    // 3. GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> getClienteById(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.getClienteById(id));
    }

    // 4. CREATE
    @PostMapping
    public ResponseEntity<Cliente> createCliente(@Valid @RequestBody Cliente cliente) {
        return ResponseEntity.ok(clienteService.createCliente(cliente));
    }

    // 5. UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Cliente> updateCliente(
            @PathVariable Long id,
            @Valid @RequestBody Cliente cliente
    ) {
        return ResponseEntity.ok(clienteService.updateCliente(id, cliente));
    }

    // 6. DESACTIVAR
    @DeleteMapping("/desactivar/{id}")
    public ResponseEntity<Void> desactivarCliente(@PathVariable Long id) {
        clienteService.desactivarCliente(id);
        return ResponseEntity.noContent().build();
    }

    // 7. ACTIVAR
    @PutMapping("/activar/{id}")
    public ResponseEntity<Cliente> activarCliente(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.activarCliente(id));
    }

    @GetMapping("/search")
    public Page<ClienteDTO> searchClientes(
        Pageable pageable,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String tipoCliente,
        @RequestParam(required = false) String tipoDocumento,
        @RequestParam(required = false) Boolean estado
    ) {
        return clienteService.searchClientes(
            pageable, search, tipoCliente, tipoDocumento, estado
        ).map(c -> mapToDTO(c));
    }

    // Mapper para no repetir código
    private ClienteDTO mapToDTO(Cliente c) {
        return new ClienteDTO(
            c.getIdCliente(),
            c.getTipoCliente(),
            c.getPrimerNombre(),
            c.getSegundoNombre(),
            c.getPrimerApellido(),
            c.getSegundoApellido(),
            c.getRazonSocial(),
            c.getIdentificadorNit(),
            c.getTipoDocumento(),
            c.getDocumento(),
            c.getEmail(),
            c.getTelefono(),
            c.getDireccion(),
            c.getEstado()
        );
    }
}
    