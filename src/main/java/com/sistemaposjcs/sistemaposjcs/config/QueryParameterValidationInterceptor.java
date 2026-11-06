package com.sistemaposjcs.sistemaposjcs.config;

import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Rechaza filtros GET que no correspondan a parámetros declarados por el endpoint. */
public class QueryParameterValidationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        if (!HttpMethod.GET.matches(request.getMethod()) || !(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        Set<String> allowedParameters = new LinkedHashSet<>();
        for (MethodParameter parameter : handlerMethod.getMethodParameters()) {
            RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
            if (requestParam != null) {
                String parameterName = requestParam.name().isBlank()
                        ? requestParam.value()
                        : requestParam.name();
                if (parameterName.isBlank()) {
                    parameterName = parameter.getParameterName();
                }
                if (parameterName != null) {
                    allowedParameters.add(parameterName);
                }
            }

            if (Pageable.class.isAssignableFrom(parameter.getParameterType())) {
                allowedParameters.add("page");
                allowedParameters.add("size");
                allowedParameters.add("sort");
            }
        }

        for (String providedParameter : request.getParameterMap().keySet()) {
            if (!allowedParameters.contains(providedParameter)) {
                String allowed = allowedParameters.isEmpty()
                        ? "este endpoint no acepta filtros"
                        : "parámetros admitidos: " + String.join(", ", allowedParameters);
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Filtro o parámetro no reconocido: '" + providedParameter + "'; " + allowed
                );
            }
        }

        return true;
    }
}
