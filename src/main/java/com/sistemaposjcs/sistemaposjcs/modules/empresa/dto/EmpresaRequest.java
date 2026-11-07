package com.sistemaposjcs.sistemaposjcs.modules.empresa.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EmpresaRequest(
        @NotBlank(message = "El nombre comercial es obligatorio")
        @Size(max = 150, message = "El nombre comercial no puede superar 150 caracteres")
        String nombreComercial,

        @NotBlank(message = "La razón social es obligatoria")
        @Size(max = 150, message = "La razón social no puede superar 150 caracteres")
        String razonSocial,

        @NotBlank(message = "El NIT es obligatorio")
        @Pattern(regexp = "^[0-9]{5,15}$", message = "El NIT debe contener entre 5 y 15 dígitos, sin guiones")
        String nit,

        @NotBlank(message = "El dígito de verificación es obligatorio")
        @Pattern(regexp = "^[0-9]$", message = "El dígito de verificación debe ser un dígito")
        String digitoVerificacion,

        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 255, message = "La dirección no puede superar 255 caracteres")
        String direccion,

        @NotBlank(message = "El municipio es obligatorio")
        @Size(max = 100, message = "El municipio no puede superar 100 caracteres")
        String municipio,

        @NotBlank(message = "El departamento es obligatorio")
        @Size(max = 100, message = "El departamento no puede superar 100 caracteres")
        String departamento,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(
                regexp = "^[0-9+()\\- ]{7,20}$",
                message = "El teléfono debe tener entre 7 y 20 caracteres válidos"
        )
        String telefono,

        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 150, message = "El correo no puede superar 150 caracteres")
        String correo,

        // Opcional durante la transicion del frontend; null conserva el valor existente.
        Boolean responsableIva
) {
}
