package com.sistemaposjcs.sistemaposjcs.modules.reglas.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.sistemaposjcs.sistemaposjcs.config.QueryParameterValidationInterceptor;
import com.sistemaposjcs.sistemaposjcs.exception.GlobalExceptionHandler;
import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.service.UsuarioAuthService;
import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.controller.PermisosController;
import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.dto.PermisoModuloResponse;
import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.interceptor.AutorizacionInterceptor;
import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.model.PermisoAccion;
import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.service.AutorizacionService;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.service.ReglaService;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.dto.PermisoCambioRequest;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ReglasHttpTest {
    private final ReglaService reglas = mock(ReglaService.class);
    private final AutorizacionService autorizacion = mock(AutorizacionService.class);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(new ReglaController(reglas),
                new PermisosController(new UsuarioAuthService(usuarios), autorizacion))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .addInterceptors(new AutorizacionInterceptor(autorizacion), new QueryParameterValidationInterceptor())
                .build();
    }

    @AfterEach
    void limpiar() { SecurityContextHolder.clearContext(); }

    private void autenticar() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("operador", null, List.of()));
    }

    @Test
    void permisosPropiosNoRequierenConsultarReglasNiAdmitenIdentidadAjena() throws Exception {
        autenticar();
        Usuario usuario = new Usuario();
        usuario.setUsername("operador");
        usuario.setEstado(true);
        when(usuarios.findByUsername("operador")).thenReturn(Optional.of(usuario));
        when(autorizacion.permisosDeUsuario("operador")).thenReturn(List.of(
                new PermisoModuloResponse("MODULOS", true, false, true, false, false)));
        mvc.perform(get("/api/auth/permisos")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigoModulo").value("MODULOS"));
        mvc.perform(get("/api/auth/permisos").param("usuarioId", "999")).andExpect(status().isBadRequest());
        verify(autorizacion).permisosDeUsuario("operador");
        verify(autorizacion, never()).tienePermiso(anyString(), anyString(), any());
    }

    @Test
    void permisosPropiosSinAutenticacionSon401() throws Exception {
        mvc.perform(get("/api/auth/permisos")).andExpect(status().isUnauthorized());
        verifyNoInteractions(autorizacion);
    }

    @Test
    void matrizYSelectoresExigenConsultaDeReglas() throws Exception {
        autenticar();
        mvc.perform(get("/api/reglas/matriz").param("usuarioId", "7")).andExpect(status().isForbidden());
        mvc.perform(get("/api/reglas/usuarios")).andExpect(status().isForbidden());
        mvc.perform(get("/api/reglas/modulos")).andExpect(status().isForbidden());
        verifyNoInteractions(reglas);
    }

    @Test
    void matrizFiltraUnUsuarioYNoRequiereConsultarSuCrud() throws Exception {
        autenticar();
        when(autorizacion.tienePermiso("operador", "REGLAS", PermisoAccion.CONSULTA)).thenReturn(true);
        when(reglas.matriz(eq(7L), isNull(), any())).thenReturn(Page.empty(org.springframework.data.domain.PageRequest.of(0, 20)));
        mvc.perform(get("/api/reglas/matriz").param("usuarioId", "7")).andExpect(status().isOk());
        mvc.perform(get("/api/reglas/matriz")).andExpect(status().isBadRequest());
        verify(autorizacion, never()).tienePermiso(eq("operador"), eq("USUARIOS"), any());
    }

    @Test
    void cambiarExigeActualizarYPermiteRevocarConsultaPropia() throws Exception {
        autenticar();
        String body = """
                {"usuarioId":7,"moduloId":3,"accion":"CONSULTA","valor":false}
                """;
        mvc.perform(patch("/api/reglas/permisos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        when(autorizacion.tienePermiso("operador", "REGLAS", PermisoAccion.ACTUALIZACION)).thenReturn(true);
        mvc.perform(patch("/api/reglas/permisos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNoContent());
        verify(reglas).cambiarPermiso(new PermisoCambioRequest(7L, 3L, PermisoAccion.CONSULTA, false));
        mvc.perform(patch("/api/reglas/permisos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuarioId\":7,\"moduloId\":3,\"accion\":\"ROL\",\"valor\":true}"))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/reglas/permisos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuarioId\":7,\"moduloId\":3,\"accion\":\"CONSULTA\"}"))
                .andExpect(status().isBadRequest());
    }
}