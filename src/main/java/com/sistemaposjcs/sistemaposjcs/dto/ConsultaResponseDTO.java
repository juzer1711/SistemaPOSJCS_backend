package com.sistemaposjcs.sistemaposjcs.dto;

import com.sistemaposjcs.sistemaposjcs.model.Enum.TipoConsulta;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaResponseDTO {

    private String pregunta;
    private TipoConsulta tipoConsulta;
    private String respuesta;
}
