package com.sistemaposjcs.sistemaposjcs.modules.autorizacion.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.annotation.ModuloProtegido;
import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.annotation.PermisoRequerido;
import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.model.PermisoAccion;
import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.service.AutorizacionService;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AutorizacionInterceptorTest {

    @Mock
    private AutorizacionService autorizacionService;

    @InjectMocks
    private AutorizacionInterceptor interceptor;

    @AfterEach
    void limpiarContextoDeSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void mapeaGetAlPermisoDeConsultaDelUsuarioAutenticado() throws Exception {
        autenticarComo("ana");
        when(autorizacionService.tienePermiso("ana", "USUARIOS", PermisoAccion.CONSULTA))
                .thenReturn(true);

        assertTrue(interceptor.preHandle(request("GET"), response(), handler("consulta")));
        verify(autorizacionService).tienePermiso("ana", "USUARIOS", PermisoAccion.CONSULTA);
    }

    @Test
    void deniegaCon403CuandoElPermisoNoEstaOtorgado() throws Exception {
        autenticarComo("ana");
        when(autorizacionService.tienePermiso("ana", "USUARIOS", PermisoAccion.ACTUALIZACION))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> interceptor.preHandle(request("PATCH"), response(), handler("actualizar"))
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void permiteDeclararLaAccionReporteSinMapearlaAConsulta() throws Exception {
        autenticarComo("ana");
        when(autorizacionService.tienePermiso("ana", "USUARIOS", PermisoAccion.REPORTE))
                .thenReturn(true);

        assertTrue(interceptor.preHandle(request("GET"), response(), handler("reporte")));
        verify(autorizacionService).tienePermiso("ana", "USUARIOS", PermisoAccion.REPORTE);
    }

    @Test
    void aplicaModuloDeclaradoDirectamenteEnElMetodo() throws Exception {
        autenticarComo("ana");
        when(autorizacionService.tienePermiso("ana", "EMPRESA", PermisoAccion.ACTUALIZACION))
                .thenReturn(true);

        assertTrue(interceptor.preHandle(request("PUT"), response(), handler("actualizarEmpresa")));
        verify(autorizacionService).tienePermiso("ana", "EMPRESA", PermisoAccion.ACTUALIZACION);
    }

    @Test
    void deniegaMetodoSinAccionMapeada() throws Exception {
        autenticarComo("ana");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> interceptor.preHandle(request("TRACE"), response(), handler("consulta"))
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void detalleRealDeModuloExigeConsultaYDeniegaSinRegla() throws Exception {
        autenticarComo("ana");
        var controller = new com.sistemaposjcs.sistemaposjcs.modules.modulos.controller.ModuloController(
                org.mockito.Mockito.mock(com.sistemaposjcs.sistemaposjcs.modules.modulos.service.ModuloService.class));
        var handler = new HandlerMethod(controller,
                controller.getClass().getMethod("obtenerPorId", Long.class));
        when(autorizacionService.tienePermiso("ana", "MODULOS", PermisoAccion.CONSULTA))
                .thenReturn(false, true);
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(ResponseStatusException.class,
                () -> interceptor.preHandle(request("GET"), response(), handler)).getStatusCode());
        assertTrue(interceptor.preHandle(request("GET"), response(), handler));
    }

    @Test
    void disponibilidadRealDeModuloExigeConsulta() throws Exception {
        autenticarComo("ana");
        var controller = new com.sistemaposjcs.sistemaposjcs.modules.modulos.controller.ModuloController(
                org.mockito.Mockito.mock(com.sistemaposjcs.sistemaposjcs.modules.modulos.service.ModuloService.class));
        var handler = new HandlerMethod(controller,
                controller.getClass().getMethod("disponibilidadCodigo", String.class));
        when(autorizacionService.tienePermiso("ana", "MODULOS", PermisoAccion.CONSULTA))
                .thenReturn(false);
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(ResponseStatusException.class,
                () -> interceptor.preHandle(request("GET"), response(), handler)).getStatusCode());
    }
    private void autenticarComo(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, List.of())
        );
    }

    private MockHttpServletRequest request(String method) {
        return new MockHttpServletRequest(method, "/api/users");
    }

    private MockHttpServletResponse response() {
        return new MockHttpServletResponse();
    }

    private HandlerMethod handler(String name) throws Exception {
        Method method = TestController.class.getMethod(name);
        return new HandlerMethod(new TestController(), method);
    }

    @ModuloProtegido("USUARIOS")
    static class TestController {
        public void consulta() {
        }

        public void actualizar() {
        }

        @PermisoRequerido(PermisoAccion.REPORTE)
        public void reporte() {
        }

        @ModuloProtegido("EMPRESA")
        public void actualizarEmpresa() {
        }
    }
}
