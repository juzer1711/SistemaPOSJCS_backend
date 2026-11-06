package com.sistemaposjcs.sistemaposjcs.modules.autorizacion.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.sistemaposjcs.sistemaposjcs.modules.reglas.model.Regla;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.repository.ReglaRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.model.PermisoAccion;

@ExtendWith(MockitoExtension.class)
class AutorizacionServiceTest {

    @Mock
    private ReglaRepository reglaRepository;

    @InjectMocks
    private AutorizacionService autorizacionService;

    @Test
    void deniegaCuandoNoHayReglaParaUsuarioYModulo() {
        when(reglaRepository.findByUsuario_UsernameAndModulo_CodigoModulo("ana", "USUARIOS"))
                .thenReturn(Optional.empty());

        assertFalse(autorizacionService.tienePermiso("ana", "USUARIOS", PermisoAccion.CONSULTA));
    }

    @Test
    void consultaLaColumnaAsociadaALaAccionSolicitada() {
        Regla regla = new Regla();
        regla.setReglaConsulta(true);
        regla.setReglaInsercion(false);
        when(reglaRepository.findByUsuario_UsernameAndModulo_CodigoModulo("ana", "USUARIOS"))
                .thenReturn(Optional.of(regla));

        assertTrue(autorizacionService.tienePermiso("ana", "USUARIOS", PermisoAccion.CONSULTA));
        assertFalse(autorizacionService.tienePermiso("ana", "USUARIOS", PermisoAccion.INSERCION));
    }

    @Test
    void deniegaCuandoElUsernameNoEstaDisponible() {
        assertFalse(autorizacionService.tienePermiso("", "USUARIOS", PermisoAccion.CONSULTA));
    }
}
