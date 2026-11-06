package com.sistemaposjcs.sistemaposjcs.model.Enum;
import java.math.BigDecimal;
public enum IVA {
    IVA_0(BigDecimal.valueOf(0.00)),
    IVA_5(BigDecimal.valueOf(0.05)),
    IVA_19(BigDecimal.valueOf(0.19));

    private final BigDecimal value;

    IVA(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getValue() {
        return value;
    }
}
