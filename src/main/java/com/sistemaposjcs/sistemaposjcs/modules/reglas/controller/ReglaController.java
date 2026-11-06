package com.sistemaposjcs.sistemaposjcs.modules.reglas.controller;

import com.sistemaposjcs.sistemaposjcs.modules.reglas.dto.ReglaRequest;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.dto.ReglaResponse;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.service.ReglaService;
import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.annotation.ModuloProtegido;
import jakarta.validation.Valid;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.dto.MatrizPermisoResponse;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.dto.ReglaUsuarioOpcion;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.dto.ReglaModuloOpcion;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.dto.PermisoCambioRequest;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/reglas")
@ModuloProtegido("REGLAS")
public class ReglaController {

    private final ReglaService reglaService;

    public ReglaController(ReglaService reglaService) {
        this.reglaService = reglaService;
    }

    @GetMapping
    public Page<ReglaResponse> listar(
            Pageable pageable,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) Long moduloId
    ) {
        return reglaService.listar(pageable, usuarioId, moduloId);
    }

    @GetMapping("/matriz")
    public Page<MatrizPermisoResponse> matriz(@RequestParam Long usuarioId,
            @RequestParam(required = false) Long moduloId, Pageable pageable) {
        return reglaService.matriz(usuarioId, moduloId, pageable);
    }

    @GetMapping("/usuarios")
    public Page<ReglaUsuarioOpcion> usuarios(@RequestParam(required = false) String search, Pageable pageable) {
        return reglaService.usuarios(search, pageable);
    }

    @GetMapping("/modulos")
    public Page<ReglaModuloOpcion> modulos(@RequestParam(required = false) String search, Pageable pageable) {
        return reglaService.modulos(search, pageable);
    }

    @PatchMapping("/permisos")
    public ResponseEntity<Void> cambiarPermiso(@Valid @RequestBody PermisoCambioRequest request) {
        reglaService.cambiarPermiso(request);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{idRegla}")
    public ReglaResponse obtenerPorId(@PathVariable Long idRegla) {
        return reglaService.obtenerPorId(idRegla);
    }

    @PostMapping
    public ResponseEntity<ReglaResponse> crear(@Valid @RequestBody ReglaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reglaService.crear(request));
    }

    @PutMapping("/{idRegla}")
    public ReglaResponse actualizar(
            @PathVariable Long idRegla,
            @Valid @RequestBody ReglaRequest request
    ) {
        return reglaService.actualizar(idRegla, request);
    }

    @DeleteMapping("/{idRegla}")
    public ResponseEntity<Void> eliminar(@PathVariable Long idRegla) {
        reglaService.eliminar(idRegla);
        return ResponseEntity.noContent().build();
    }
}
