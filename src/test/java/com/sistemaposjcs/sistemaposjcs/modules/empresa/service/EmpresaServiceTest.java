package com.sistemaposjcs.sistemaposjcs.modules.empresa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.service.UsuarioAuthService;
import com.sistemaposjcs.sistemaposjcs.modules.empresa.dto.EmpresaRequest;
import com.sistemaposjcs.sistemaposjcs.modules.empresa.model.Empresa;
import com.sistemaposjcs.sistemaposjcs.modules.empresa.repository.EmpresaRepository;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario;
import com.sistemaposjcs.sistemaposjcs.service.AuditoriaService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    private static final String SETUP_TOKEN = "12345678901234567890123456789012";

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private EmpresaLogoStorage logoStorage;

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private UsuarioAuthService usuarioAuthService;

    @InjectMocks
    private EmpresaService empresaService;

    @Test
    void crearEmpresaRequiereTokenDeBootstrapValido() {
        EmpresaService service = serviceConToken();
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.crearEmpresa(request(), "token-incorrecto")
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verify(empresaRepository, never()).saveAndFlush(any(Empresa.class));
    }

    @Test
    void crearEmpresaSoloPermiteUnRegistroPorInstalacion() {
        EmpresaService service = serviceConToken();
        when(empresaRepository.count()).thenReturn(1L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.crearEmpresa(request(), SETUP_TOKEN)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(empresaRepository, never()).saveAndFlush(any(Empresa.class));
    }

    @Test
    void creaUnaEmpresaConTokenCorrectoSinExigirSesionDeUsuario() {
        EmpresaService service = serviceConToken();
        when(empresaRepository.count()).thenReturn(0L);
        when(empresaRepository.saveAndFlush(any(Empresa.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.crearEmpresa(request(), SETUP_TOKEN);

        assertEquals("Comercial POS", response.nombreComercial());
        assertEquals("900123456", response.nit());
        assertEquals("7", response.digitoVerificacion());
        verify(auditoriaService, never()).registrar(any(), any(), any(), any());
    }

    @Test
    void actualizarEmpresaAuditaElCambioSinGuardarValoresTributariosEnLaDescripcion() {
        Empresa empresa = empresaExistente();
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(empresaRepository.saveAndFlush(empresa)).thenReturn(empresa);
        Usuario administrador = new Usuario();
        when(usuarioAuthService.getUsuarioLogueado()).thenReturn(administrador);

        empresaService.actualizarEmpresa(1L, request());

        verify(auditoriaService).registrar(
                administrador,
                "EMPRESA",
                "ACTUALIZAR",
                "Se actualizaron los campos: nombreComercial"
        );
    }

    @Test
    void obtenerEmpresaDevuelveNotFoundCuandoAunNoSeHaRegistrado() {
        when(empresaRepository.findFirstByOrderByIdEmpresaAsc()).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> empresaService.obtenerEmpresa()
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    private EmpresaService serviceConToken() {
        return new EmpresaService(
                empresaRepository,
                logoStorage,
                auditoriaService,
                usuarioAuthService,
                SETUP_TOKEN
        );
    }

    private EmpresaRequest request() {
        return new EmpresaRequest(
                "Comercial POS",
                "Comercial POS S.A.S.",
                "900123456",
                "7",
                "Calle 10 # 5-20",
                "Bogotá",
                "Cundinamarca",
                "+57 601 5551234",
                "contacto@example.com"
        );
    }

    private Empresa empresaExistente() {
        Empresa empresa = new Empresa();
        empresa.setIdEmpresa(1L);
        empresa.setNombreComercial("Nombre anterior");
        empresa.setRazonSocial("Comercial POS S.A.S.");
        empresa.setNit("900123456");
        empresa.setDigitoVerificacion("7");
        empresa.setDireccion("Calle 10 # 5-20");
        empresa.setMunicipio("Bogotá");
        empresa.setDepartamento("Cundinamarca");
        empresa.setTelefono("+57 601 5551234");
        empresa.setCorreo("contacto@example.com");
        return empresa;
    }
}
