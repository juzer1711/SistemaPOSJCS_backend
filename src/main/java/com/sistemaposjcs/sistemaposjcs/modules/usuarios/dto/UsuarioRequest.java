package com.sistemaposjcs.sistemaposjcs.modules.usuarios.dto;

import com.sistemaposjcs.sistemaposjcs.model.Enum.TipoDocumento;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Contrato de entrada y validaciones comunes para crear y actualizar usuarios. */
@Getter
@Setter
@NoArgsConstructor
public class UsuarioRequest {

    public interface OnCreate extends Default {}
    public interface OnUpdate extends Default {}

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El usuario debe tener entre 3 y 50 caracteres")
    private String username;

    @NotBlank(groups = OnCreate.class, message = "La contraseña es obligatoria")
    @Size(min = 6, max = 72, message = "La contraseña debe tener entre 6 y 72 caracteres")
    private String password;

    @NotBlank(message = "El primer nombre es obligatorio")
    @Size(max = 100, message = "El primer nombre no puede superar 100 caracteres")
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$", message = "El primer nombre solo puede contener letras")
    private String primerNombre;

    @Size(max = 100, message = "El segundo nombre no puede superar 100 caracteres")
    @Pattern(regexp = "^$|^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$", message = "El segundo nombre solo puede contener letras")
    private String segundoNombre;

    @NotBlank(message = "El primer apellido es obligatorio")
    @Size(max = 100, message = "El primer apellido no puede superar 100 caracteres")
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$", message = "El primer apellido solo puede contener letras")
    private String primerApellido;

    @Size(max = 100, message = "El segundo apellido no puede superar 100 caracteres")
    @Pattern(regexp = "^$|^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$", message = "El segundo apellido solo puede contener letras")
    private String segundoApellido;

    @NotNull(message = "El tipo de documento es obligatorio")
    private TipoDocumento tipoDocumento;

    @NotBlank(message = "El documento es obligatorio")
    @Pattern(regexp = "^[0-9]{6,12}$", message = "El documento debe contener de 6 a 12 dígitos numéricos")
    private String documento;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 150, message = "El correo no puede superar 150 caracteres")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{7,10}$", message = "El teléfono debe contener de 7 a 10 dígitos")
    private String telefono;

}
