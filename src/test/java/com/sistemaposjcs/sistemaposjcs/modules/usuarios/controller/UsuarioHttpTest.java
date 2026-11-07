package com.sistemaposjcs.sistemaposjcs.modules.usuarios.controller;

import com.sistemaposjcs.sistemaposjcs.modules.usuarios.service.UsuarioService;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.service.UsuarioExportService;
import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.interceptor.AutorizacionInterceptor;
import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.service.AutorizacionService;
import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.model.PermisoAccion;
import com.sistemaposjcs.sistemaposjcs.config.QueryParameterValidationInterceptor;
import com.sistemaposjcs.sistemaposjcs.exception.GlobalExceptionHandler;
import java.util.List;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.domain.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UsuarioHttpTest {
    private final UsuarioService users = mock(UsuarioService.class);
    private final UsuarioExportService exports = mock(UsuarioExportService.class);
    private final AutorizacionService auth = mock(AutorizacionService.class);
    private MockMvc mvc;
    @BeforeEach void setup() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("prueba", null, List.of()));
        mvc = MockMvcBuilders.standaloneSetup(new UsuarioController(users), new UsuarioExportController(exports))
                .setControllerAdvice(new GlobalExceptionHandler()).setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .addInterceptors(new AutorizacionInterceptor(auth), new QueryParameterValidationInterceptor()).build();
    }
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }
    @Test void consultaProtegeListadoYCatalogo() throws Exception {
        mvc.perform(get("/api/users/search")).andExpect(status().isForbidden());
        mvc.perform(get("/api/users/tipos-documento")).andExpect(status().isForbidden());
        when(auth.tienePermiso("prueba", "USUARIOS", PermisoAccion.CONSULTA)).thenReturn(true);
        when(users.searchUsuarios(any(), any(), any(), any())).thenReturn(Page.empty(PageRequest.of(0, 20)));
        mvc.perform(get("/api/users/search").param("estado", "false")).andExpect(status().isOk());
        mvc.perform(get("/api/users/tipos-documento")).andExpect(status().isOk())
                .andExpect(jsonPath("$[4]").value("NIT")).andExpect(jsonPath("$[5]").value("PEP"));
        mvc.perform(get("/api/users/search").param("rol", "ADMIN")).andExpect(status().isBadRequest());
    }
    @Test void estadoRequiereActualizacionNoEliminar() throws Exception {
        mvc.perform(patch("/api/users/desactivar/7")).andExpect(status().isForbidden());
        when(auth.tienePermiso("prueba", "USUARIOS", PermisoAccion.ACTUALIZACION)).thenReturn(true);
        mvc.perform(patch("/api/users/desactivar/7")).andExpect(status().isNoContent());
        when(users.activarUsuario(7L)).thenReturn(new com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario());
        mvc.perform(patch("/api/users/activar/7")).andExpect(status().isOk());
        verify(users).desactivarUsuario(7L); verify(users).activarUsuario(7L);
        verify(auth, never()).tienePermiso(any(), any(), eq(PermisoAccion.DELETE));
    }
    @Test void exportacionesExigenReporteYConservanRutaYFiltros() throws Exception {
        mvc.perform(get("/api/export/usuarios/csv")).andExpect(status().isForbidden());
        mvc.perform(get("/api/export/usuarios/excel")).andExpect(status().isForbidden());
        when(auth.tienePermiso("prueba", "USUARIOS", PermisoAccion.REPORTE)).thenReturn(true);
        when(exports.csv(any(), any(), any())).thenReturn("ID,Username\n".getBytes());
        mvc.perform(get("/api/export/usuarios/csv").param("estado", "false").param("search", "ana"))
                .andExpect(status().isOk()).andExpect(header().string("Content-Disposition", "attachment; filename=usuarios.csv"));
        verify(exports).csv("ana", false, null);
        mvc.perform(get("/api/export/usuarios/csv").param("tipoDocumento", "NO_EXISTE")).andExpect(status().isBadRequest());
    }
}
