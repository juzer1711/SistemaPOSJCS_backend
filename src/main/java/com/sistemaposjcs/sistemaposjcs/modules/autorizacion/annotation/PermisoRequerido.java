package com.sistemaposjcs.sistemaposjcs.modules.autorizacion.annotation;

import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.model.PermisoAccion;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PermisoRequerido {
    PermisoAccion value();
}
