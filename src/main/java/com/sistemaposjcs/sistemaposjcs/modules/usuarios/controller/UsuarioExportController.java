package com.sistemaposjcs.sistemaposjcs.modules.usuarios.controller;

import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.annotation.ModuloProtegido;
import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.annotation.PermisoRequerido;
import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.model.PermisoAccion;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.service.UsuarioExportService;
import com.sistemaposjcs.sistemaposjcs.model.Enum.TipoDocumento;
import java.io.IOException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/export/usuarios")
@ModuloProtegido("USUARIOS")
public class UsuarioExportController {
    private final UsuarioExportService exports;
    public UsuarioExportController(UsuarioExportService exports) { this.exports = exports; }

    @GetMapping("/excel")
    @PermisoRequerido(PermisoAccion.REPORTE)
    public ResponseEntity<byte[]> excel(@RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean estado, @RequestParam(required = false) TipoDocumento tipoDocumento) throws IOException {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=usuarios.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(exports.excel(search, estado, tipoDocumento));
    }

    @GetMapping("/csv")
    @PermisoRequerido(PermisoAccion.REPORTE)
    public ResponseEntity<byte[]> csv(@RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean estado, @RequestParam(required = false) TipoDocumento tipoDocumento) {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=usuarios.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(exports.csv(search, estado, tipoDocumento));
    }
}