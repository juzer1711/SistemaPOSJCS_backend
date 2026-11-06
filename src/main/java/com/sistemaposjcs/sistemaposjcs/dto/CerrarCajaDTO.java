package com.sistemaposjcs.sistemaposjcs.dto;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class CerrarCajaDTO {

    private Long idUsuario;
    private BigDecimal efectivoReal;
    private BigDecimal transferenciaReal;

}