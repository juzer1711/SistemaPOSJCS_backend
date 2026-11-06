package com.sistemaposjcs.sistemaposjcs.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReporteCajeroDTO {

    private String cajero;

    private BigDecimal ventas;
}