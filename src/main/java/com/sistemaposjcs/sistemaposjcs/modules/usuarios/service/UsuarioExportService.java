package com.sistemaposjcs.sistemaposjcs.modules.usuarios.service;

import com.sistemaposjcs.sistemaposjcs.model.Enum.TipoDocumento;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.dto.UsuarioResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class UsuarioExportService {
    private final UsuarioService usuarios;
    public UsuarioExportService(UsuarioService usuarios) { this.usuarios = usuarios; }

    public byte[] excel(String search, Boolean estado, TipoDocumento tipoDocumento) throws IOException {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Usuarios");
            String[] headers = {"ID", "Username", "Nombre Completo", "Tipo Documento", "Documento", "Email", "Teléfono", "Estado"};
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont(); font.setBold(true); style.setFont(font);
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]); header.getCell(i).setCellStyle(style);
                sheet.setColumnWidth(i, (i == 2 || i == 5 ? 35 : 24) * 256);
            }
            int[] index = {1};
            usuarios.recorrerUsuarios(search, estado, tipoDocumento, usuario -> {
                Row row = sheet.createRow(index[0]++);
                String[] values = values(usuario);
                row.createCell(0).setCellValue(usuario.idUsuario());
                for (int i = 1; i < values.length; i++) row.createCell(i).setCellValue(values[i]);
            });
            workbook.write(output);
            return output.toByteArray();
        }
    }

    public byte[] csv(String search, Boolean estado, TipoDocumento tipoDocumento) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(output, false, StandardCharsets.UTF_8);
        writer.println("ID,Username,NombreCompleto,TipoDocumento,Documento,Email,Telefono,Estado");
        usuarios.recorrerUsuarios(search, estado, tipoDocumento, usuario -> {
            String[] values = values(usuario);
            for (int i = 0; i < values.length; i++) {
                if (i > 0) writer.print(',');
                writer.print('"'); writer.print(values[i].replace("\"", "\"\"")); writer.print('"');
            }
            writer.println();
        });
        writer.flush();
        return output.toByteArray();
    }

    private String[] values(UsuarioResponse usuario) {
        return new String[]{String.valueOf(usuario.idUsuario()), usuario.username(), usuario.nombreCompleto(),
                usuario.tipoDocumento().name(), usuario.documento(), usuario.email() == null ? "" : usuario.email(),
                usuario.telefono() == null ? "" : usuario.telefono(), Boolean.TRUE.equals(usuario.estado()) ? "Activo" : "Inactivo"};
    }
}