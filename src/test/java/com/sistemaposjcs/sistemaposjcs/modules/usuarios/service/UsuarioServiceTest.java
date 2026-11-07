package com.sistemaposjcs.sistemaposjcs.modules.usuarios.service;

import com.sistemaposjcs.sistemaposjcs.modules.usuarios.dto.UsuarioRequest;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.repository.UsuarioRepository;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.service.ReglaService;
import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.service.UsuarioAuthService;
import com.sistemaposjcs.sistemaposjcs.service.AuditoriaService;
import com.sistemaposjcs.sistemaposjcs.model.Enum.TipoDocumento;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {
    private final UsuarioRepository repository = mock(UsuarioRepository.class);
    private final AuditoriaService auditoria = mock(AuditoriaService.class);
    private final UsuarioAuthService auth = mock(UsuarioAuthService.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final ReglaService reglas = mock(ReglaService.class);
    private final UsuarioService service = new UsuarioService(repository, auditoria, auth, encoder, reglas);

    private UsuarioRequest request() {
        UsuarioRequest request = new UsuarioRequest();
        request.setUsername("usuario"); request.setDocumento("12345678"); request.setPrimerNombre("Ana");
        request.setPrimerApellido("Pérez"); request.setEmail("ana@example.test"); request.setTelefono("3001234567");
        request.setTipoDocumento(TipoDocumento.CEDULA_CIUDADANIA);
        return request;
    }
    private Usuario existing() {
        Usuario usuario = new Usuario(); usuario.setIdUsuario(7L); usuario.setDocumento("12345678");
        usuario.setPassword("hash-previo-de-prueba"); usuario.setUsername("usuario"); usuario.setPrimerNombre("Ana");
        usuario.setPrimerApellido("Pérez"); usuario.setEstado(true);
        when(repository.findByIdForUpdate(7L)).thenReturn(Optional.of(usuario));
        when(repository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(auth.getUsuarioLogueado()).thenReturn(usuario);
        return usuario;
    }
    @Test
    void documentoEsInmutableSinGuardarNiAuditar() {
        Usuario usuario = new Usuario(); usuario.setDocumento("12345678");
        when(repository.findByIdForUpdate(7L)).thenReturn(Optional.of(usuario));
        UsuarioRequest request = request(); request.setDocumento("99999999");
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class, () -> service.updateUser(7L, request)).getStatusCode());
        verify(repository, never()).save(any()); verifyNoInteractions(auditoria);
    }
    @Test
    void editarSinPasswordConservaHashYNormalizaCampos() {
        existing(); UsuarioRequest request = request(); request.setPrimerNombre(" Ana ");
        Usuario saved = service.updateUser(7L, request);
        assertEquals("Ana", saved.getPrimerNombre()); assertEquals("hash-previo-de-prueba", saved.getPassword());
        verifyNoInteractions(encoder);
    }
    @Test
    void cambioDePasswordAuditaSinIncluirCredenciales() {
        existing(); UsuarioRequest request = request(); request.setPassword("clave-solo-de-prueba");
        when(encoder.encode("clave-solo-de-prueba")).thenReturn("nuevo-hash-de-prueba");
        assertEquals("nuevo-hash-de-prueba", service.updateUser(7L, request).getPassword());
        verify(auditoria).registrar(any(), eq("USUARIO"), eq("ACTUALIZAR"), argThat(text -> text.contains("Contraseña actualizada") && !text.contains("clave-solo-de-prueba") && !text.contains("nuevo-hash")));
    }
    @Test
    void crearAsignaReglasFalseYEstadoActivo() {
        when(repository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(auth.getUsuarioLogueado()).thenReturn(new Usuario());
        when(encoder.encode(anyString())).thenReturn("hash-de-prueba");
        UsuarioRequest request = request(); request.setPassword("clave-prueba");
        Usuario saved = service.createUser(request);
        assertTrue(saved.getEstado()); verify(reglas).crearReglasFalseParaUsuario(saved);
    }
    @Test
    void desactivacionPropiaCapturaOperadorAntesDeDesactivar() {
        Usuario usuario = existing(); service.desactivarUsuario(7L);
        assertFalse(usuario.getEstado());
        var order = inOrder(auth, repository, auditoria);
        order.verify(auth).getUsuarioLogueado(); order.verify(repository).save(usuario);
        order.verify(auditoria).registrar(eq(usuario), eq("USUARIO"), eq("DESACTIVAR"), anyString());
    }
    @Test
    void activarConservaRelacionesYUsaElMismoUsuario() {
        Usuario usuario = existing(); usuario.setEstado(false);
        assertSame(usuario, service.activarUsuario(7L)); assertTrue(usuario.getEstado());
        verifyNoInteractions(reglas);
    }
    @Test
    void ordenCalculadoSeTraduceConDesempateYTamanioLimitado() {
        Pageable page = service.limitarPageable(PageRequest.of(2, 300, Sort.by(Sort.Order.desc("nombreCompleto"))));
        assertEquals(100, page.getPageSize()); assertEquals("primerNombre", page.getSort().iterator().next().getProperty());
        assertNotNull(page.getSort().getOrderFor("idUsuario"));
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> service.limitarPageable(PageRequest.of(0, 10, Sort.by("password")))).getStatusCode());
    }
    @Test
    void rechazaBusquedaExcesivaAntesDeConsultar() {
        assertThrows(ResponseStatusException.class, () -> service.buildSpec("x".repeat(151), null, null));
        verifyNoInteractions(repository);
    }
}
