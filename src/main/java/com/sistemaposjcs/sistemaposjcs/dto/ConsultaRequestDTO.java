package com.sistemaposjcs.sistemaposjcs.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsultaRequestDTO {

    @NotBlank(message = "La pregunta no puede estar vacia")
    private String pregunta;
}
