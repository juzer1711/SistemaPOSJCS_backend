package com.sistemaposjcs.sistemaposjcs.modules.empresa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;

import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.service.UsuarioAuthService;
import com.sistemaposjcs.sistemaposjcs.modules.empresa.dto.EmpresaRequest;
import com.sistemaposjcs.sistemaposjcs.modules.empresa.model.Empresa;
import com.sistemaposjcs.sistemaposjcs.modules.empresa.repository.EmpresaRepository;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario;
import com.sistemaposjcs.sistemaposjcs.service.AuditoriaService;
import java.util.Optional;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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

    @AfterEach
    void limpiarSincronizaciones() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

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
        when(empresaRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(empresa));
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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void registroInicialPersisteLaCondicionTributariaExplicita(boolean responsableIva) {
        when(empresaRepository.saveAndFlush(any(Empresa.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = serviceConToken().crearEmpresa(request(responsableIva), SETUP_TOKEN);

        assertEquals(responsableIva, response.responsableIva());
        verify(empresaRepository).saveAndFlush(org.mockito.ArgumentMatchers.argThat(
                empresa -> Boolean.valueOf(responsableIva).equals(empresa.getResponsableIva())));
    }

    @Test
    void registroLegacySinCondicionQuedaPendienteSinAsumirNoResponsable() {
        when(empresaRepository.saveAndFlush(any(Empresa.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertNull(serviceConToken().crearEmpresa(request(), SETUP_TOKEN).responsableIva());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void cambiarCondicionTributariaSePersisteYAudita(boolean responsableIva) {
        Empresa empresa = empresaExistente();
        empresa.setNombreComercial("Comercial POS");
        empresa.setResponsableIva(!responsableIva);
        when(empresaRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(empresa));
        when(empresaRepository.saveAndFlush(empresa)).thenReturn(empresa);
        Usuario administrador = new Usuario();
        when(usuarioAuthService.getUsuarioLogueado()).thenReturn(administrador);

        var response = empresaService.actualizarEmpresa(1L, request(responsableIva));

        assertEquals(responsableIva, empresa.getResponsableIva());
        assertEquals(responsableIva, response.responsableIva());
        verify(auditoriaService).registrar(administrador, "EMPRESA", "ACTUALIZAR",
                "Se actualizaron los campos: responsableIva");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void solicitudLegacyNoBorraLaCondicionConfigurada(boolean responsableIva) {
        Empresa empresa = empresaExistente();
        empresa.setNombreComercial("Comercial POS");
        empresa.setResponsableIva(responsableIva);
        when(empresaRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(empresa));
        when(empresaRepository.saveAndFlush(empresa)).thenReturn(empresa);

        assertEquals(responsableIva, empresaService.actualizarEmpresa(1L, request()).responsableIva());
        verifyNoInteractions(auditoriaService, usuarioAuthService);
    }

    @Test
    void consultaEmpresaExistenteSinCondicionDevuelvePendiente() {
        when(empresaRepository.findFirstByOrderByIdEmpresaAsc()).thenReturn(Optional.of(empresaExistente()));
        assertNull(empresaService.obtenerEmpresa().responsableIva());
    }

    @Test
    void repetirCondicionTributariaNoGeneraAuditoriaDeCambio() {
        Empresa empresa = empresaExistente();
        empresa.setNombreComercial("Comercial POS");
        empresa.setResponsableIva(true);
        when(empresaRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(empresa));
        when(empresaRepository.saveAndFlush(empresa)).thenReturn(empresa);

        assertEquals(true, empresaService.actualizarEmpresa(1L, request(true)).responsableIva());
        verifyNoInteractions(auditoriaService, usuarioAuthService);
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

    @Test
    void actualizarDatosConservaLogoYEstado() {
        Empresa empresa = empresaExistente();
        empresa.setLogo("https://example.com/logo.png");
        empresa.setLogoPublicId("logo-anterior");
        when(empresaRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(empresa));
        when(empresaRepository.saveAndFlush(empresa)).thenReturn(empresa);
        var response = empresaService.actualizarEmpresa(1L, request());
        assertEquals("https://example.com/logo.png", response.logo());
        assertEquals("logo-anterior", empresa.getLogoPublicId());
        assertEquals(true, response.estado());
        verifyNoInteractions(logoStorage);
    }

    @Test
    void logoSoloEliminaElAnteriorTrasConfirmarYConservaLosDatos() throws IOException {
        Empresa empresa = prepararLogo();
        var response = empresaService.actualizarLogo(archivoLogo());
        assertEquals("https://example.com/nuevo.png", response.logo());
        assertEquals("Nombre anterior", response.nombreComercial());
        assertEquals("900123456", response.nit());
        assertEquals(true, response.responsableIva());
        verify(logoStorage, never()).delete(any());
        var synchronization = TransactionSynchronizationManager.getSynchronizations().getFirst();
        synchronization.afterCommit();
        synchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
        verify(logoStorage).delete("logo-anterior");
        verify(logoStorage, never()).delete("logo-nuevo");
        verify(empresaRepository).saveAndFlush(empresa);
    }

    @Test
    void rollbackAlGuardarLogoLimpiaElNuevoYConservaElAnterior() throws IOException {
        Empresa empresa = prepararLogo();
        when(empresaRepository.saveAndFlush(empresa)).thenThrow(new DataIntegrityViolationException("fallo simulado"));
        assertThrows(DataIntegrityViolationException.class, () -> empresaService.actualizarLogo(archivoLogo()));
        TransactionSynchronizationManager.getSynchronizations().getFirst()
                .afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
        verify(logoStorage).delete("logo-nuevo");
        verify(logoStorage, never()).delete("logo-anterior");
    }

    @Test
    void falloPosteriorDeAuditoriaTambienLimpiaElNuevoEnRollback() throws IOException {
        prepararLogo();
        doThrow(new IllegalStateException("fallo simulado")).when(auditoriaService)
                .registrar(any(), any(), any(), any());
        assertThrows(IllegalStateException.class, () -> empresaService.actualizarLogo(archivoLogo()));
        TransactionSynchronizationManager.getSynchronizations().getFirst()
                .afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
        verify(logoStorage).delete("logo-nuevo");
        verify(logoStorage, never()).delete("logo-anterior");
    }

    @Test
    void falloAlEliminarAnteriorNoConvierteUnCommitEnError() throws IOException {
        prepararLogo();
        empresaService.actualizarLogo(archivoLogo());
        doThrow(new IOException("fallo simulado")).when(logoStorage).delete("logo-anterior");
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                TransactionSynchronizationManager.getSynchronizations().getFirst().afterCommit());
        verify(logoStorage, never()).delete("logo-nuevo");
    }

    private Empresa prepararLogo() throws IOException {
        Empresa empresa = empresaExistente();
        empresa.setResponsableIva(true);
        empresa.setLogoPublicId("logo-anterior");
        when(empresaRepository.findFirstWithLockByOrderByIdEmpresaAsc()).thenReturn(Optional.of(empresa));
        when(logoStorage.upload(any())).thenReturn(new EmpresaLogoStorage.LogoAsset(
                "https://example.com/nuevo.png", "logo-nuevo"));
        TransactionSynchronizationManager.initSynchronization();
        return empresa;
    }

    private MockMultipartFile archivoLogo() {
        return new MockMultipartFile("file", "logo.png", "image/png", new byte[] {1});
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
        return request(null);
    }

    private EmpresaRequest request(Boolean responsableIva) {
        return new EmpresaRequest(
                "Comercial POS",
                "Comercial POS S.A.S.",
                "900123456",
                "7",
                "Calle 10 # 5-20",
                "Bogotá",
                "Cundinamarca",
                "+57 601 5551234",
                "contacto@example.com",
                responsableIva
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
