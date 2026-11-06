package com.sistemaposjcs.sistemaposjcs.modules.empresa.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

class EmpresaRequestValidationTest {

    @Test
    void acceptsValidCompanyProfile() {
        EmpresaRequest request = request(
                "Comercial POS",
                "Comercial POS S.A.S.",
                "900123456",
                "7",
                "Calle 10 # 5-20",
                "Bogotá",
                "Cundinamarca",
                "+57 601 5551234",
                "contacto@example.com"
        );

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            assertTrue(validator.validate(request).isEmpty());
        }
    }

    @Test
    void rejectsNitWithSeparatorsAndInvalidVerificationDigit() {
        EmpresaRequest request = request(
                "Comercial POS",
                "Comercial POS S.A.S.",
                "900123456-7",
                "77",
                "Calle 10 # 5-20",
                "Bogotá",
                "Cundinamarca",
                "6015551234",
                null
        );

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            assertTrue(validator.validate(request).stream()
                    .anyMatch(error -> error.getPropertyPath().toString().equals("nit")));
            assertTrue(validator.validate(request).stream()
                    .anyMatch(error -> error.getPropertyPath().toString().equals("digitoVerificacion")));
        }
    }

    private EmpresaRequest request(
            String nombreComercial,
            String razonSocial,
            String nit,
            String digitoVerificacion,
            String direccion,
            String municipio,
            String departamento,
            String telefono,
            String correo
    ) {
        return new EmpresaRequest(
                nombreComercial,
                razonSocial,
                nit,
                digitoVerificacion,
                direccion,
                municipio,
                departamento,
                telefono,
                correo
        );
    }
}
