package com.sistemaposjcs.sistemaposjcs.modules.modulos.controller;

import com.sistemaposjcs.sistemaposjcs.modules.modulos.dto.ModuloRequest;
import com.sistemaposjcs.sistemaposjcs.modules.modulos.dto.ModuloResponse;
import com.sistemaposjcs.sistemaposjcs.modules.modulos.service.ModuloService;
import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.annotation.ModuloProtegido;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/modulos")
@ModuloProtegido("MODULOS")
public class ModuloController {

    private final ModuloService moduloService;

    public ModuloController(ModuloService moduloService) {
        this.moduloService = moduloService;
    }

    @GetMapping
    public Page<ModuloResponse> listar(Pageable pageable) {
        return moduloService.listar(pageable).map(ModuloResponse::from);
    }

    @GetMapping("/search")
    public Page<ModuloResponse> buscar(
            Pageable pageable,
            @RequestParam(required = false) String nombreModulo,
            @RequestParam(required = false) String zona
    ) {
        return moduloService.buscar(pageable, nombreModulo, zona).map(ModuloResponse::from);
    }

    @GetMapping("/disponibilidad-codigo")
    public Map<String, Boolean> disponibilidadCodigo(@RequestParam String codigoModulo) {
        return Map.of("disponible", moduloService.codigoDisponible(codigoModulo));
    }

    @GetMapping("/{idModulo}")
    public ModuloResponse obtenerPorId(@PathVariable Long idModulo) {
        return ModuloResponse.from(moduloService.obtenerPorId(idModulo));
    }

    @PostMapping
    public ResponseEntity<ModuloResponse> crear(@Valid @RequestBody ModuloRequest request) {
        ModuloResponse response = ModuloResponse.from(moduloService.crear(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{idModulo}")
    public ModuloResponse actualizar(
            @PathVariable Long idModulo,
            @Valid @RequestBody ModuloRequest request
    ) {
        return ModuloResponse.from(moduloService.actualizar(idModulo, request));
    }

    @DeleteMapping("/{idModulo}")
    public ResponseEntity<Void> eliminar(@PathVariable Long idModulo) {
        moduloService.eliminar(idModulo);
        return ResponseEntity.noContent().build();
    }
}
