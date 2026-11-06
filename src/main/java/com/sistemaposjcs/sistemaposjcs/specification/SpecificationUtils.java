package com.sistemaposjcs.sistemaposjcs.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;

public class SpecificationUtils {

    private SpecificationUtils() {}

    public static Expression<String> nombreCompleto(
            CriteriaBuilder cb,
            Path<?> root
    ) {
        Expression<String> primerNombre = cb.lower(cb.coalesce(root.get("primerNombre"), ""));
        Expression<String> segundoNombre = cb.lower(cb.coalesce(root.get("segundoNombre"), ""));
        Expression<String> primerApellido = cb.lower(cb.coalesce(root.get("primerApellido"), ""));
        Expression<String> segundoApellido = cb.lower(cb.coalesce(root.get("segundoApellido"), ""));

        return cb.concat(
                        cb.concat(
                                cb.concat(primerNombre, segundoNombre),
                                primerApellido
                        ),
                        segundoApellido
        );
    }
}