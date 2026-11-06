package com.sistemaposjcs.sistemaposjcs.modules.modulos.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

class ModuloRequestValidationTest {

    @Test
    void commentsAreOptionalWhenRequiredFieldsArePresent() {
        ModuloRequest request = new ModuloRequest(
                "PRODUCTOS",
                "ProductoController.java",
                "Productos",
                "Catálogo",
                null
        );

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();

            assertTrue(validator.validate(request).isEmpty());
        }
    }

    @Test
    void requiredFieldsCannotBeBlank() {
        ModuloRequest request = new ModuloRequest(" ", " ", " ", " ", null);

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();

            assertTrue(validator.validate(request).stream()
                    .anyMatch(error -> error.getPropertyPath().toString().equals("codigoModulo")));
            assertTrue(validator.validate(request).stream()
                    .anyMatch(error -> error.getPropertyPath().toString().equals("archivo")));
            assertTrue(validator.validate(request).stream()
                    .anyMatch(error -> error.getPropertyPath().toString().equals("nombreModulo")));
            assertTrue(validator.validate(request).stream()
                    .anyMatch(error -> error.getPropertyPath().toString().equals("zona")));
        }
    }
}
