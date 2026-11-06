package com.sistemaposjcs.sistemaposjcs.dto;

import com.sistemaposjcs.sistemaposjcs.modules.usuarios.dto.UsuarioAuditoriaDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaDTO {

    private Long id;

    private LocalDateTime fecha;

    private UsuarioAuditoriaDTO usuario;

    private String modulo;

    private String accion;

    private String descripcion;
}
