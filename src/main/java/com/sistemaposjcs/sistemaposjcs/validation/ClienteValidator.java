package com.sistemaposjcs.sistemaposjcs.validation;

import com.sistemaposjcs.sistemaposjcs.model.Cliente;
import com.sistemaposjcs.sistemaposjcs.model.Enum.TipoCliente;
import com.sistemaposjcs.sistemaposjcs.model.Enum.TipoDocumento;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class ClienteValidator implements ConstraintValidator<ValidCliente, Cliente> {

    @Override
    public boolean isValid(Cliente cliente, ConstraintValidatorContext context) {

        if (cliente == null) return true;
        context.disableDefaultConstraintViolation();

        boolean valid = true; // ← Flag para saber si hubo errores

        // ======================
        // PERSONA NATURAL
        // ======================
        if (cliente.getTipoCliente() == TipoCliente.PERSONA_NATURAL) {

            // Obligatorios
            if (isBlank(cliente.getPrimerNombre())) {
                addError("primerNombre", "El primer nombre es obligatorio", context);
                valid = false;
            }

            if (isBlank(cliente.getPrimerApellido())) {
                addError("primerApellido", "El primer apellido es obligatorio", context);
                valid = false;
            }

            // No permitidos
            if (!isBlank(cliente.getRazonSocial())) {
                addError("razonSocial", "Una persona natural no debe tener razón social", context);
                valid = false;
            }

            if (!isBlank(cliente.getIdentificadorNit())) {
                addError("identificadorNit", "Una persona natural no debe tener identificador NIT", context);
                valid = false;
            }

            // Documento no válido
            if (cliente.getTipoDocumento() == TipoDocumento.NIT) {
                addError("tipoDocumento", "Una persona natural no puede tener tipo de documento NIT", context);
                valid = false;
            }
        }

        // ======================
        // EMPRESA
        // ======================
        if (cliente.getTipoCliente() == TipoCliente.EMPRESA) {

            // Obligatorios
            if (isBlank(cliente.getRazonSocial())) {
                addError("razonSocial", "La razón social es obligatoria", context);
                valid = false;
            }

            if (isBlank(cliente.getIdentificadorNit())) {
                addError("identificadorNit", "El dígito verificador del NIT es obligatorio", context);
                valid = false;
            }

            // No permitidos
            if (!isBlank(cliente.getPrimerNombre())) {
                addError("primerNombre", "Una empresa no debe tener primer nombre", context);
                valid = false;
            }

            if (!isBlank(cliente.getSegundoNombre())) {
                addError("segundoNombre", "Una empresa no debe tener segundo nombre", context);
                valid = false;
            }

            if (!isBlank(cliente.getPrimerApellido())) {
                addError("primerApellido", "Una empresa no debe tener primer apellido", context);
                valid = false;
            }

            if (!isBlank(cliente.getSegundoApellido())) {
                addError("segundoApellido", "Una empresa no debe tener segundo apellido", context);
                valid = false;
            }

            // Tipo documento obligatorio
            if (cliente.getTipoDocumento() != TipoDocumento.NIT) {
                addError("tipoDocumento", "El documento de una empresa debe ser NIT", context);
                valid = false;
            }
        }

        return valid; // ← Esto es lo que se debe retornar
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void addError(String field, String message, ConstraintValidatorContext context) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
    }
}


