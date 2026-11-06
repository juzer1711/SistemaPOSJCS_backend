package com.sistemaposjcs.sistemaposjcs.modules.empresa.controller;

import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.annotation.ModuloProtegido;
import com.sistemaposjcs.sistemaposjcs.modules.empresa.dto.EmpresaRequest;
import com.sistemaposjcs.sistemaposjcs.modules.empresa.dto.EmpresaResponse;
import com.sistemaposjcs.sistemaposjcs.modules.empresa.service.EmpresaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/empresa")
@Validated
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping
    public ResponseEntity<EmpresaResponse> obtenerEmpresa() {
        return ResponseEntity.ok(empresaService.obtenerEmpresa());
    }

    @PostMapping
    public ResponseEntity<EmpresaResponse> crearEmpresa(
            @RequestHeader(name = "X-Empresa-Setup-Token", required = false) String setupToken,
            @Valid @RequestBody EmpresaRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(empresaService.crearEmpresa(request, setupToken));
    }

    @PutMapping("/{idEmpresa}")
    @ModuloProtegido("EMPRESA")
    public ResponseEntity<EmpresaResponse> actualizarEmpresa(
            @PathVariable Long idEmpresa,
            @Valid @RequestBody EmpresaRequest request
    ) {
        return ResponseEntity.ok(empresaService.actualizarEmpresa(idEmpresa, request));
    }

    @PostMapping("/logo")
    @ModuloProtegido("EMPRESA")
    public ResponseEntity<EmpresaResponse> subirLogo(@RequestParam("file") MultipartFile file)
            throws java.io.IOException {
        return ResponseEntity.ok(empresaService.actualizarLogo(file));
    }
}
