package com.sistemaposjcs.sistemaposjcs.modules.usuarios.service;

import com.sistemaposjcs.sistemaposjcs.modules.usuarios.dto.UsuarioResponse;
import com.sistemaposjcs.sistemaposjcs.model.Enum.TipoDocumento;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

class UsuarioExportServiceTest {
    @Test void exportaCamposPublicosYEscapaCsv() throws Exception {
        UsuarioService service = mock(UsuarioService.class);
        UsuarioResponse value = new UsuarioResponse(7L, "usuario,\"prueba\"", "Ana", null, "Pérez", null, "Ana Pérez",
                TipoDocumento.NIT, "12345678", "ana@example.test", "3001234567", false);
        doAnswer(invocation -> { ((Consumer<UsuarioResponse>) invocation.getArgument(3)).accept(value); return null; })
                .when(service).recorrerUsuarios(any(), any(), any(), any());
        UsuarioExportService exports = new UsuarioExportService(service);
        String csv = new String(exports.csv("ana", false, TipoDocumento.NIT), StandardCharsets.UTF_8);
        assertTrue(csv.contains("\"usuario,\"\"prueba\"\"\"")); assertTrue(csv.contains("Inactivo")); assertFalse(csv.contains("password"));
        try (var workbook = WorkbookFactory.create(new ByteArrayInputStream(exports.excel("ana", false, TipoDocumento.NIT)))) {
            assertEquals("Nombre Completo", workbook.getSheetAt(0).getRow(0).getCell(2).getStringCellValue());
            assertEquals("Ana Pérez", workbook.getSheetAt(0).getRow(1).getCell(2).getStringCellValue());
        }
        verify(service, times(2)).recorrerUsuarios(eq("ana"), eq(false), eq(TipoDocumento.NIT), any());
    }
}