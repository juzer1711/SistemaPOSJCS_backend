package com.sistemaposjcs.sistemaposjcs.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ClienteValidator.class)
public @interface ValidCliente {

    String message() default "Datos del cliente inválidos";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}
