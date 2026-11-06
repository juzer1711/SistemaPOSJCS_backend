package com.sistemaposjcs.sistemaposjcs.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReporteProductoDTO {

    private String producto;

    private Long cantidad;
}