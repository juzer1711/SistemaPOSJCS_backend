package com.sistemaposjcs.sistemaposjcs.modules.reglas.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

class ReglaRequestValidationTest {

    @Test
    void requiresUserModuleAndEveryPermission() {
        ReglaRequest request = new ReglaRequest(null, null, null, null, null, null, null);

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();

            assertTrue(validator.validate(request).stream()
                    .anyMatch(error -> error.getPropertyPath().toString().equals("usuarioId")));
            assertTrue(validator.validate(request).stream()
                    .anyMatch(error -> error.getPropertyPath().toString().equals("moduloId")));
            assertTrue(validator.validate(request).stream()
                    .anyMatch(error -> error.getPropertyPath().toString().equals("reglaConsulta")));
            assertTrue(validator.validate(request).stream()
                    .anyMatch(error -> error.getPropertyPath().toString().equals("reglaInsercion")));
            assertTrue(validator.validate(request).stream()
                    .anyMatch(error -> error.getPropertyPath().toString().equals("reglaActualizacion")));
            assertTrue(validator.validate(request).stream()
                    .anyMatch(error -> error.getPropertyPath().toString().equals("reglaReporte")));
            assertTrue(validator.validate(request).stream()
                    .anyMatch(error -> error.getPropertyPath().toString().equals("reglaDelete")));
        }
    }

    @Test
    void falseIsAValidPermissionValue() {
        ReglaRequest request = new ReglaRequest(1L, 1L, false, false, false, false, false);

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();

            assertTrue(validator.validate(request).isEmpty());
        }
    }
}
