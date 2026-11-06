package com.sistemaposjcs.sistemaposjcs.modules.reglas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sistemaposjcs.sistemaposjcs.modules.modulos.model.Modulo;
import com.sistemaposjcs.sistemaposjcs.modules.modulos.repository.ModuloRepository;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.dto.ReglaRequest;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.model.Regla;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.repository.ReglaRepository;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.dto.PermisoCambioRequest;
import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.model.PermisoAccion;
import static org.mockito.Mockito.inOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ReglaServiceTest {

    @Mock
    private ReglaRepository reglaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ModuloRepository moduloRepository;

    @InjectMocks
    private ReglaService reglaService;

    @Test
    void crearGuardaPermisosIndividualesDelUsuario() {
        Usuario usuario = usuario(7L);
        Modulo modulo = modulo(3L);
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario));
        when(moduloRepository.findById(3L)).thenReturn(Optional.of(modulo));
        when(reglaRepository.existsByUsuario_IdUsuarioAndModulo_IdModulo(7L, 3L)).thenReturn(false);
        when(reglaRepository.save(any(Regla.class))).thenAnswer(invocation -> invocation.getArgument(0));

        reglaService.crear(new ReglaRequest(7L, 3L,  true, false, true, false, true));

        ArgumentCaptor<Regla> reglaCaptor = ArgumentCaptor.forClass(Regla.class);
        verify(reglaRepository).save(reglaCaptor.capture());
        Regla regla = reglaCaptor.getValue();
        assertEquals(usuario, regla.getUsuario());
        assertEquals(modulo, regla.getModulo());
        assertFalse(regla.isReglaInsercion());
        assertFalse(regla.isReglaReporte());
    }

    @Test
    void crearRechazaReglaDuplicadaParaUsuarioYModulo() {
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario(7L)));
        when(moduloRepository.findById(3L)).thenReturn(Optional.of(modulo(3L)));
        when(reglaRepository.existsByUsuario_IdUsuarioAndModulo_IdModulo(7L, 3L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reglaService.crear(new ReglaRequest(7L, 3L, true, false, false, false, false))
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(reglaRepository, never()).save(any(Regla.class));
    }

    @Test
    void nuevoUsuarioGeneraReglasEnBaseSinCargarTodosLosModulos() {
        reglaService.crearReglasFalseParaUsuario(usuario(7L));
        verify(reglaRepository).crearFalseParaUsuario(7L);
        verify(moduloRepository, never()).findAll();
    }

    @Test
    void nuevoModuloGeneraReglasEnBaseSinCargarUsuariosNiCredenciales() {
        reglaService.crearReglasFalseParaModulo(modulo(3L));
        verify(reglaRepository).crearFalseParaModulo(3L);
        verify(usuarioRepository, never()).findAll();
    }

    @ParameterizedTest
    @EnumSource(PermisoAccion.class)
    void cambiarEnviaSoloLaAccionElegidaYCompletaRelacionFaltante(
            PermisoAccion accion) {
        when(usuarioRepository.existsById(7L)).thenReturn(true);
        when(moduloRepository.existsById(3L)).thenReturn(true);
        when(reglaRepository.cambiarPermiso(7L, 3L, accion.name(), true)).thenReturn(1);
        reglaService.cambiarPermiso(new PermisoCambioRequest(7L, 3L, accion, true));
        var orden = inOrder(reglaRepository);
        orden.verify(reglaRepository).asegurarRegla(7L, 3L);
        orden.verify(reglaRepository).cambiarPermiso(7L, 3L, accion.name(), true);
        verify(reglaRepository, never()).save(any());
    }

    @Test
    void matrizNoAceptaUnUsuarioInexistente() {
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ResponseStatusException.class,
                () -> reglaService.matriz(9L, null, PageRequest.of(0, 10))).getStatusCode());
        verify(reglaRepository, never()).matriz(any(), any(), any());
    }

    @Test
    void matrizLimitaTamanoYConservaFiltros() {
        when(usuarioRepository.existsById(7L)).thenReturn(true);
        when(reglaRepository.matriz(any(), any(), any())).thenReturn(Page.empty());
        reglaService.matriz(7L, 3L, PageRequest.of(2, 300));
        verify(reglaRepository).matriz(7L, 3L, PageRequest.of(2, 100));
    }

    @Test
    void selectorEscapaComodinesYNoCargaUsuariosCompletos() {
        reglaService.usuarios(" Op%_ ", PageRequest.of(0, 20));
        verify(reglaRepository).usuarios("op\\%\\_%", PageRequest.of(0, 20));
        verify(usuarioRepository, never()).findAll();
    }

    private Usuario usuario(Long idUsuario) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(idUsuario);
        return usuario;
    }

    private Modulo modulo(Long idModulo) {
        Modulo modulo = new Modulo();
        modulo.setIdModulo(idModulo);
        return modulo;
    }
}
