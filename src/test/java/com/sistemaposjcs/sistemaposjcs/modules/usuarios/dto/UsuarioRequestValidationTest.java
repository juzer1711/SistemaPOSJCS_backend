package com.sistemaposjcs.sistemaposjcs.modules.usuarios.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

class UsuarioRequestValidationTest {

    @Test
    void passwordIsRequiredOnCreateAndOptionalOnUpdate() {
        UsuarioRequest request = validRequest();
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();

            assertTrue(validator.validate(request, UsuarioRequest.OnUpdate.class).isEmpty());

            var createErrors = validator.validate(request, UsuarioRequest.OnCreate.class);
            assertTrue(createErrors.stream().anyMatch(error -> error.getPropertyPath().toString().equals("password")));
        }
    }

    private UsuarioRequest validRequest() {
        UsuarioRequest request = new UsuarioRequest();
        request.setUsername("usuario01");
        request.setPrimerNombre("Ana");
        request.setPrimerApellido("Perez");
        request.setTipoDocumento(
                com.sistemaposjcs.sistemaposjcs.model.Enum.TipoDocumento.CEDULA_CIUDADANIA
        );
        request.setDocumento("12345678");
        request.setEmail("ana@example.com");
        request.setTelefono("3001234567");
        return request;
    }
}
