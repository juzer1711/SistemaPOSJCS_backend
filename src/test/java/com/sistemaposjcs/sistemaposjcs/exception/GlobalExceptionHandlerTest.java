package com.sistemaposjcs.sistemaposjcs.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

class GlobalExceptionHandlerTest {

    @Test
    void responseStatusPreservaStatusMensajeYPath() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/999");

        var response = handler.handleResponseStatus(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"),
                request
        );

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Usuario no encontrado", response.getBody().get("message"));
        assertEquals("/api/users/999", response.getBody().get("path"));
    }
}
