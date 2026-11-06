package com.sistemaposjcs.sistemaposjcs.modules.autorizacion.interceptor;

import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.annotation.ModuloProtegido;
import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.annotation.PermisoRequerido;
import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.model.PermisoAccion;
import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.service.AutorizacionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AutorizacionInterceptor implements HandlerInterceptor {

    private final AutorizacionService autorizacionService;

    public AutorizacionInterceptor(AutorizacionService autorizacionService) {
        this.autorizacionService = autorizacionService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        ModuloProtegido modulo = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(),
                ModuloProtegido.class
        );
        if (modulo == null) {
            modulo = AnnotatedElementUtils.findMergedAnnotation(
                    handlerMethod.getBeanType(),
                    ModuloProtegido.class
            );
        }
        if (modulo == null) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Se requiere autenticación");
        }

        PermisoRequerido permisoExplicito = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(),
                PermisoRequerido.class
        );
        PermisoAccion accion = permisoExplicito == null
                ? accionPorMetodo(request.getMethod())
                : permisoExplicito.value();

        if (accion == null || !autorizacionService.tienePermiso(
                authentication.getName(),
                modulo.value(),
                accion
        )) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permiso para realizar esta operación"
            );
        }

        return true;
    }

    private PermisoAccion accionPorMetodo(String metodo) {
        return switch (metodo) {
            case "GET", "HEAD" -> PermisoAccion.CONSULTA;
            case "POST" -> PermisoAccion.INSERCION;
            case "PUT", "PATCH" -> PermisoAccion.ACTUALIZACION;
            case "DELETE" -> PermisoAccion.DELETE;
            default -> null;
        };
    }
}
