package com.sistemaposjcs.sistemaposjcs.modules.empresa.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.sistemaposjcs.sistemaposjcs.exception.GlobalExceptionHandler;
import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.interceptor.AutorizacionInterceptor;
import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.model.PermisoAccion;
import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.service.AutorizacionService;
import com.sistemaposjcs.sistemaposjcs.modules.empresa.dto.EmpresaResponse;
import com.sistemaposjcs.sistemaposjcs.modules.empresa.dto.EmpresaRequest;
import com.sistemaposjcs.sistemaposjcs.modules.empresa.service.EmpresaService;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Contrato MVC e interceptor reales; identidad, servicio y autorización simulados. */
class EmpresaHttpTest {
    private final EmpresaService empresa = mock(EmpresaService.class);
    private final AutorizacionService autorizacion = mock(AutorizacionService.class);
    private MockMvc mvc;
    private static final String DATOS = """
            {"nombreComercial":"Comercial POS","razonSocial":"Comercial POS S.A.S.",
            "nit":"900123456","digitoVerificacion":"7","direccion":"Calle 10",
            "municipio":"Bogotá","departamento":"Cundinamarca","telefono":"555-1234","correo":null}
            """;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(new EmpresaController(empresa))
                .setControllerAdvice(new GlobalExceptionHandler())
                .addInterceptors(new AutorizacionInterceptor(autorizacion)).build();
    }

    @AfterEach
    void limpiar() { SecurityContextHolder.clearContext(); }

    @Test
    void consultaPublicaNoExigeSesionNiConsultaEmpresa() throws Exception {
        when(empresa.obtenerEmpresa()).thenReturn(response());
        mvc.perform(get("/api/empresa")).andExpect(status().isOk())
                .andExpect(jsonPath("$.idEmpresa").value(1))
                .andExpect(jsonPath("$.responsableIva").value(false))
                .andExpect(jsonPath("$.logoPublicId").doesNotExist());
        verifyNoInteractions(autorizacion);
    }

    @Test
    void registroInicialSigueDelegandoElTokenSinExigirRegla() throws Exception {
        when(empresa.crearEmpresa(any(), eq("instalacion-simulada"))).thenReturn(response());
        mvc.perform(post("/api/empresa").header("X-Empresa-Setup-Token", "instalacion-simulada")
                .contentType(MediaType.APPLICATION_JSON).content(DATOS))
                .andExpect(status().isCreated());
        verify(empresa).crearEmpresa(any(), eq("instalacion-simulada"));
        verifyNoInteractions(autorizacion);
    }

    @Test
    void escriturasSinSesionSon401() throws Exception {
        mvc.perform(put("/api/empresa/1").contentType(MediaType.APPLICATION_JSON).content(DATOS))
                .andExpect(status().isUnauthorized());
        mvc.perform(multipart("/api/empresa/logo").file(logo())).andExpect(status().isUnauthorized());
        verifyNoInteractions(empresa);
    }

    @Test
    void consultaOInsercionNoConcedenActualizarDatosNiLogo() throws Exception {
        autenticar();
        // Por defecto no hay ACTUALIZACION, aunque el usuario tenga otros permisos.
        lenient().when(autorizacion.tienePermiso("operador", "EMPRESA", PermisoAccion.CONSULTA)).thenReturn(true);
        lenient().when(autorizacion.tienePermiso("operador", "EMPRESA", PermisoAccion.INSERCION)).thenReturn(true);
        mvc.perform(put("/api/empresa/1").contentType(MediaType.APPLICATION_JSON).content(DATOS))
                .andExpect(status().isForbidden());
        mvc.perform(multipart("/api/empresa/logo").file(logo())).andExpect(status().isForbidden());
        verifyNoInteractions(empresa);
        verify(autorizacion, times(2)).tienePermiso("operador", "EMPRESA", PermisoAccion.ACTUALIZACION);
    }

    @Test
    void actualizarDatosYLogoUsanActualizacionIndependienteDeConsultaEInsercion() throws Exception {
        autenticar();
        when(autorizacion.tienePermiso("operador", "EMPRESA", PermisoAccion.ACTUALIZACION)).thenReturn(true);
        when(empresa.actualizarEmpresa(eq(1L), any())).thenReturn(response());
        when(empresa.actualizarLogo(any())).thenReturn(response());
        mvc.perform(put("/api/empresa/1").contentType(MediaType.APPLICATION_JSON).content(DATOS))
                .andExpect(status().isOk()).andExpect(jsonPath("$.digitoVerificacion").value("7"));
        mvc.perform(multipart("/api/empresa/logo").file(logo())).andExpect(status().isOk())
                .andExpect(jsonPath("$.logo").value("https://example.com/logo.png"));
        verify(autorizacion, never()).tienePermiso(any(), any(), eq(PermisoAccion.INSERCION));
        verify(autorizacion, never()).tienePermiso(any(), any(), eq(PermisoAccion.CONSULTA));
    }

    @Test
    void camposObligatoriosFaltantesDevuelvenErroresAsociadosAlFormulario() throws Exception {
        autenticar();
        when(autorizacion.tienePermiso("operador", "EMPRESA", PermisoAccion.ACTUALIZACION)).thenReturn(true);
        mvc.perform(put("/api/empresa/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.digitoVerificacion").exists())
                .andExpect(jsonPath("$.fieldErrors.municipio").exists())
                .andExpect(jsonPath("$.fieldErrors.departamento").exists());
        verifyNoInteractions(empresa);
    }

    @Test
    void actualizarCondicionTributariaRequiereActualizacion() throws Exception {
        autenticar();
        String datos = DATOS.replace("\"correo\":null", "\"correo\":null,\"responsableIva\":true");
        mvc.perform(put("/api/empresa/1").contentType(MediaType.APPLICATION_JSON).content(datos))
                .andExpect(status().isForbidden());
        verifyNoInteractions(empresa);
    }

    @Test
    void actualizarRecibeFalseExplicitoSinConfundirloConCampoAusente() throws Exception {
        autenticar();
        when(autorizacion.tienePermiso("operador", "EMPRESA", PermisoAccion.ACTUALIZACION)).thenReturn(true);
        when(empresa.actualizarEmpresa(eq(1L), any())).thenReturn(response());
        String datos = DATOS.replace("\"correo\":null", "\"correo\":null,\"responsableIva\":false");
        mvc.perform(put("/api/empresa/1").contentType(MediaType.APPLICATION_JSON).content(datos))
                .andExpect(status().isOk()).andExpect(jsonPath("$.responsableIva").value(false));
        verify(empresa).actualizarEmpresa(eq(1L), argThat(
                (EmpresaRequest request) -> Boolean.FALSE.equals(request.responsableIva())));
    }

    @Test
    void condicionTributariaInvalidaDevuelve400SinEjecutarServicio() throws Exception {
        autenticar();
        when(autorizacion.tienePermiso("operador", "EMPRESA", PermisoAccion.ACTUALIZACION)).thenReturn(true);
        String datos = DATOS.replace("\"correo\":null", "\"correo\":null,\"responsableIva\":\"pendiente\"");
        mvc.perform(put("/api/empresa/1").contentType(MediaType.APPLICATION_JSON).content(datos))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(empresa);
    }

    private void autenticar() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("operador", null, List.of()));
    }

    private MockMultipartFile logo() {
        return new MockMultipartFile("file", "logo.png", "image/png", new byte[] {1});
    }

    private EmpresaResponse response() {
        return new EmpresaResponse(1L, "Comercial POS", "Comercial POS S.A.S.", "900123456", "7",
                "Calle 10", "Bogotá", "Cundinamarca", "555-1234", null,
                "https://example.com/logo.png", true, false);
    }
}
