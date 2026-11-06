package com.sistemaposjcs.sistemaposjcs.config;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;

class QueryParameterValidationInterceptorTest {

    @Test
    void aceptaFiltrosDeclaradosYPaginacion() throws Exception {
        QueryParameterValidationInterceptor interceptor = new QueryParameterValidationInterceptor();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/items/search");
        request.setParameter("search", "caja");
        request.setParameter("page", "0");
        request.setParameter("size", "20");
        request.setParameter("sort", "nombre,asc");

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), searchHandler()));
    }

    @Test
    void rechazaNombreDeFiltroDesconocidoConBadRequest() throws Exception {
        QueryParameterValidationInterceptor interceptor = new QueryParameterValidationInterceptor();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/items/search");
        request.setParameter("searh", "caja");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> interceptor.preHandle(request, new MockHttpServletResponse(), searchHandler())
        );

        assertTrue(exception.getReason().contains("searh"));
        assertTrue(exception.getStatusCode().value() == HttpServletResponse.SC_BAD_REQUEST);
    }

    private HandlerMethod searchHandler() throws NoSuchMethodException {
        Method method = TestController.class.getMethod("search", String.class, Pageable.class);
        return new HandlerMethod(new TestController(), method);
    }

    static class TestController {
        public void search(@RequestParam("search") String search, Pageable pageable) {}
    }
}
